package com.farmatodo.product.service;

import com.farmatodo.product.dto.ProductResponse;
import com.farmatodo.product.entity.Product;
import com.farmatodo.product.entity.ProductSearch;
import com.farmatodo.product.repository.ProductRepository;
import com.farmatodo.product.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de productos y búsqueda.
 * Maneja búsqueda de productos y registro de búsquedas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductSearchRepository productSearchRepository;

    @Value("${app.min-stock-threshold:0}")
    private Integer minStockThreshold;

    /**
     * Busca productos por término de búsqueda.
     * Guarda la búsqueda de forma asíncrona.
     * @param query Término de búsqueda
     * @param customerId ID del cliente (opcional)
     * @return Lista de productos encontrados
     */
    public List<ProductResponse> searchProducts(String query, UUID customerId) {
        log.info("Buscando productos con query: {}", query);

        List<Product> products = productRepository.searchProducts(query, minStockThreshold);
        List<ProductResponse> response = products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        // Guardar búsqueda de forma asíncrona
        saveSearchAsync(query, customerId, response.size());

        return response;
    }

    /**
     * Obtiene todos los productos con stock disponible.
     * @return Lista de productos disponibles
     */
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .filter(p -> p.getStock() > minStockThreshold)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un producto por su ID.
     * Valida que tenga stock disponible.
     * @param id ID del producto
     * @return Producto encontrado
     */
    public ProductResponse getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
        
        if (product.getStock() <= minStockThreshold) {
            throw new RuntimeException("Producto no disponible (stock insuficiente)");
        }
        
        return mapToResponse(product);
    }

    /**
     * Guarda una búsqueda de forma asíncrona para análisis.
     * @param query Término de búsqueda
     * @param customerId ID del cliente (opcional)
     * @param resultsCount Cantidad de resultados encontrados
     */
    @Async
    @Transactional
    public void saveSearchAsync(String query, UUID customerId, Integer resultsCount) {
        try {
            ProductSearch search = ProductSearch.builder()
                    .searchQuery(query)
                    .customerId(customerId)
                    .searchedAt(LocalDateTime.now())
                    .resultsCount(resultsCount)
                    .build();
            productSearchRepository.save(search);
            log.info("Búsqueda guardada asíncronamente: {}", query);
        } catch (Exception e) {
            log.error("Error al guardar búsqueda", e);
        }
    }

    /**
     * Convierte una entidad Product a ProductResponse.
     * @param product Entidad del producto
     * @return DTO de respuesta
     */
    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .category(product.getCategory())
                .sku(product.getSku())
                .build();
    }
}

