package com.farmatodo.product.service;

import com.farmatodo.product.dto.ProductResponse;
import com.farmatodo.product.entity.Product;
import com.farmatodo.product.entity.ProductSearch;
import com.farmatodo.product.repository.ProductRepository;
import com.farmatodo.product.repository.ProductSearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductSearchRepository productSearchRepository;

    @InjectMocks
    private ProductService productService;

    private UUID productId;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(productService, "minStockThreshold", 0);
        productId = UUID.randomUUID();
        customerId = UUID.randomUUID();
    }

    @Test
    void testSearchProducts_Success() {
        // Arrange
        String query = "aspirina";
        Product product = Product.builder()
                .id(productId)
                .name("Aspirina 500mg")
                .description("Analgésico")
                .price(BigDecimal.valueOf(5000))
                .stock(100)
                .category("Medicamentos")
                .sku("ASP-500")
                .build();

        when(productRepository.searchProducts(anyString(), anyInt()))
                .thenReturn(List.of(product));
        when(productSearchRepository.save(any(ProductSearch.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<ProductResponse> results = productService.searchProducts(query, customerId);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Aspirina 500mg", results.get(0).getName());
        assertEquals(BigDecimal.valueOf(5000), results.get(0).getPrice());
        assertEquals(100, results.get(0).getStock());
        verify(productRepository, times(1)).searchProducts(anyString(), anyInt());
    }

    @Test
    void testSearchProducts_EmptyResults() {
        // Arrange
        String query = "producto_inexistente";
        when(productRepository.searchProducts(anyString(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(productSearchRepository.save(any(ProductSearch.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<ProductResponse> results = productService.searchProducts(query, customerId);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(productRepository, times(1)).searchProducts(anyString(), anyInt());
    }

    @Test
    void testSearchProducts_MultipleProducts() {
        // Arrange
        String query = "medicamento";
        Product product1 = Product.builder()
                .id(UUID.randomUUID())
                .name("Medicamento A")
                .price(BigDecimal.valueOf(10000))
                .stock(50)
                .build();
        Product product2 = Product.builder()
                .id(UUID.randomUUID())
                .name("Medicamento B")
                .price(BigDecimal.valueOf(15000))
                .stock(30)
                .build();

        when(productRepository.searchProducts(anyString(), anyInt()))
                .thenReturn(List.of(product1, product2));
        when(productSearchRepository.save(any(ProductSearch.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<ProductResponse> results = productService.searchProducts(query, customerId);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        verify(productRepository, times(1)).searchProducts(anyString(), anyInt());
    }

    @Test
    void testSearchProducts_WithMinStockThreshold() {
        // Arrange
        ReflectionTestUtils.setField(productService, "minStockThreshold", 10);
        String query = "producto";
        Product product = Product.builder()
                .id(productId)
                .name("Producto")
                .stock(15)
                .build();

        when(productRepository.searchProducts(anyString(), eq(10)))
                .thenReturn(List.of(product));
        when(productSearchRepository.save(any(ProductSearch.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<ProductResponse> results = productService.searchProducts(query, customerId);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        verify(productRepository, times(1)).searchProducts(anyString(), eq(10));
    }

    @Test
    void testSearchProducts_SavesSearchAsync() {
        // Arrange
        String query = "test";
        when(productRepository.searchProducts(anyString(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(productSearchRepository.save(any(ProductSearch.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        productService.searchProducts(query, customerId);

        // Assert
        ArgumentCaptor<ProductSearch> searchCaptor = ArgumentCaptor.forClass(ProductSearch.class);
        verify(productSearchRepository, times(1)).save(searchCaptor.capture());
        ProductSearch savedSearch = searchCaptor.getValue();
        assertEquals(query, savedSearch.getSearchQuery());
        assertEquals(customerId, savedSearch.getCustomerId());
        assertEquals(0, savedSearch.getResultsCount());
    }

    @Test
    void testSearchProducts_SaveSearchException() {
        // Arrange
        String query = "test";
        when(productRepository.searchProducts(anyString(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(productSearchRepository.save(any(ProductSearch.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act - No debe lanzar excepción
        List<ProductResponse> results = productService.searchProducts(query, customerId);

        // Assert
        assertNotNull(results);
        verify(productSearchRepository, times(1)).save(any(ProductSearch.class));
    }

    @Test
    void testGetAllProducts_Success() {
        // Arrange
        Product product1 = Product.builder()
                .id(UUID.randomUUID())
                .name("Producto 1")
                .stock(20)
                .build();
        Product product2 = Product.builder()
                .id(UUID.randomUUID())
                .name("Producto 2")
                .stock(5)
                .build();
        Product product3 = Product.builder()
                .id(UUID.randomUUID())
                .name("Producto 3")
                .stock(0)
                .build();

        when(productRepository.findAll()).thenReturn(List.of(product1, product2, product3));

        // Act
        List<ProductResponse> results = productService.getAllProducts();

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size()); // Solo productos con stock > 0
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void testGetAllProducts_WithMinStockThreshold() {
        // Arrange
        ReflectionTestUtils.setField(productService, "minStockThreshold", 10);
        Product product1 = Product.builder()
                .id(UUID.randomUUID())
                .name("Producto 1")
                .stock(15)
                .build();
        Product product2 = Product.builder()
                .id(UUID.randomUUID())
                .name("Producto 2")
                .stock(5)
                .build();

        when(productRepository.findAll()).thenReturn(List.of(product1, product2));

        // Act
        List<ProductResponse> results = productService.getAllProducts();

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size()); // Solo producto con stock > 10
        assertEquals("Producto 1", results.get(0).getName());
    }

    @Test
    void testGetAllProducts_Empty() {
        // Arrange
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<ProductResponse> results = productService.getAllProducts();

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void testGetProductById_Success() {
        // Arrange
        Product product = Product.builder()
                .id(productId)
                .name("Producto Test")
                .description("Descripción")
                .price(BigDecimal.valueOf(10000))
                .stock(50)
                .category("Categoría")
                .sku("SKU-001")
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // Act
        ProductResponse response = productService.getProductById(productId);

        // Assert
        assertNotNull(response);
        assertEquals(productId, response.getId());
        assertEquals("Producto Test", response.getName());
        assertEquals("Descripción", response.getDescription());
        assertEquals(BigDecimal.valueOf(10000), response.getPrice());
        assertEquals(50, response.getStock());
        assertEquals("Categoría", response.getCategory());
        assertEquals("SKU-001", response.getSku());
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void testGetProductById_NotFound() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.getProductById(productId);
        });
        assertTrue(exception.getMessage().contains("no encontrado"));
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void testGetProductById_InsufficientStock() {
        // Arrange
        ReflectionTestUtils.setField(productService, "minStockThreshold", 10);
        Product product = Product.builder()
                .id(productId)
                .name("Producto")
                .stock(5)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.getProductById(productId);
        });
        assertTrue(exception.getMessage().contains("no disponible"));
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void testGetProductById_StockAtThreshold() {
        // Arrange
        ReflectionTestUtils.setField(productService, "minStockThreshold", 10);
        Product product = Product.builder()
                .id(productId)
                .name("Producto")
                .stock(10)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.getProductById(productId);
        });
        assertTrue(exception.getMessage().contains("no disponible"));
    }

    @Test
    void testSaveSearchAsync_Success() {
        // Arrange
        String query = "test query";
        Integer resultsCount = 5;
        when(productSearchRepository.save(any(ProductSearch.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        productService.saveSearchAsync(query, customerId, resultsCount);

        // Assert
        ArgumentCaptor<ProductSearch> searchCaptor = ArgumentCaptor.forClass(ProductSearch.class);
        verify(productSearchRepository, times(1)).save(searchCaptor.capture());
        ProductSearch savedSearch = searchCaptor.getValue();
        assertEquals(query, savedSearch.getSearchQuery());
        assertEquals(customerId, savedSearch.getCustomerId());
        assertEquals(resultsCount, savedSearch.getResultsCount());
        assertNotNull(savedSearch.getSearchedAt());
    }

    @Test
    void testSaveSearchAsync_ExceptionHandling() {
        // Arrange
        String query = "test query";
        when(productSearchRepository.save(any(ProductSearch.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act - No debe lanzar excepción
        productService.saveSearchAsync(query, customerId, 0);

        // Assert
        verify(productSearchRepository, times(1)).save(any(ProductSearch.class));
    }

    @Test
    void testMapToResponse_AllFields() {
        // Arrange
        Product product = Product.builder()
                .id(productId)
                .name("Producto Completo")
                .description("Descripción completa")
                .price(BigDecimal.valueOf(25000))
                .stock(100)
                .category("Categoría Test")
                .sku("SKU-TEST-001")
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // Act
        ProductResponse response = productService.getProductById(productId);

        // Assert
        assertNotNull(response);
        assertEquals(productId, response.getId());
        assertEquals("Producto Completo", response.getName());
        assertEquals("Descripción completa", response.getDescription());
        assertEquals(BigDecimal.valueOf(25000), response.getPrice());
        assertEquals(100, response.getStock());
        assertEquals("Categoría Test", response.getCategory());
        assertEquals("SKU-TEST-001", response.getSku());
    }
}

