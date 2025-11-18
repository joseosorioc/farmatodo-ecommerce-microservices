package com.farmatodo.product.controller;

import com.farmatodo.product.dto.ProductResponse;
import com.farmatodo.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product", description = "API para gestión de productos y búsqueda")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Health check", description = "Verifica el estado del servicio")
    @ApiResponse(responseCode = "200", description = "Servicio activo")
    @GetMapping("/ping")
    public ResponseEntity<Object> ping() {
        return ResponseEntity.ok(java.util.Map.of("message", "pong"));
    }

    @Operation(summary = "Buscar productos", description = "Busca productos por nombre, descripción o categoría")
    @ApiResponse(responseCode = "200", description = "Lista de productos encontrados")
    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(
            @Parameter(description = "Término de búsqueda") @RequestParam String query,
            @Parameter(description = "ID del cliente (opcional)") @RequestParam(required = false) UUID customerId) {
        List<ProductResponse> products = productService.searchProducts(query, customerId);
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Obtener todos los productos", description = "Retorna la lista de todos los productos disponibles")
    @ApiResponse(responseCode = "200", description = "Lista de productos")
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Obtener producto por ID", description = "Retorna la información de un producto específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado o sin stock")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(
            @Parameter(description = "ID del producto") @PathVariable UUID id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }
}

