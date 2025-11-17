package com.farmatodo.product.service;

import com.farmatodo.product.entity.Product;
import com.farmatodo.product.repository.ProductRepository;
import com.farmatodo.product.repository.ProductSearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
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

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(productService, "minStockThreshold", 0);
    }

    @Test
    void testSearchProducts_Success() {
        // Arrange
        String query = "aspirina";
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Aspirina 500mg");
        product.setPrice(BigDecimal.valueOf(5000));
        product.setStock(100);

        when(productRepository.searchProducts(anyString(), anyInt()))
                .thenReturn(Arrays.asList(product));

        // Act
        List<com.farmatodo.product.dto.ProductResponse> results = 
            productService.searchProducts(query, UUID.randomUUID());

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Aspirina 500mg", results.get(0).getName());
        verify(productRepository, times(1)).searchProducts(anyString(), anyInt());
    }
}

