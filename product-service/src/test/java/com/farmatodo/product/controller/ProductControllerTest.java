package com.farmatodo.product.controller;

import com.farmatodo.product.dto.ProductResponse;
import com.farmatodo.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    void testPing() throws Exception {
        mockMvc.perform(get("/api/v1/products/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("pong"));
    }

    @Test
    void testSearchProducts() throws Exception {
        // Arrange
        String query = "aspirina";
        UUID customerId = UUID.randomUUID();
        ProductResponse product = ProductResponse.builder()
                .id(UUID.randomUUID())
                .name("Aspirina 500mg")
                .description("Analgésico")
                .price(BigDecimal.valueOf(5000))
                .stock(100)
                .category("Medicamentos")
                .sku("ASP-500")
                .build();

        when(productService.searchProducts(eq(query), eq(customerId)))
                .thenReturn(List.of(product));

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/search")
                        .param("query", query)
                        .param("customerId", customerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Aspirina 500mg"))
                .andExpect(jsonPath("$[0].price").value(5000));
    }

    @Test
    void testSearchProducts_WithoutCustomerId() throws Exception {
        // Arrange
        String query = "medicamento";
        ProductResponse product = ProductResponse.builder()
                .id(UUID.randomUUID())
                .name("Medicamento")
                .price(BigDecimal.valueOf(10000))
                .stock(50)
                .build();

        when(productService.searchProducts(eq(query), eq(null)))
                .thenReturn(List.of(product));

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/search")
                        .param("query", query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testSearchProducts_EmptyResults() throws Exception {
        // Arrange
        String query = "inexistente";
        UUID customerId = UUID.randomUUID();

        when(productService.searchProducts(eq(query), eq(customerId)))
                .thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/search")
                        .param("query", query)
                        .param("customerId", customerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testSearchProducts_MultipleResults() throws Exception {
        // Arrange
        String query = "producto";
        ProductResponse product1 = ProductResponse.builder()
                .id(UUID.randomUUID())
                .name("Producto 1")
                .price(BigDecimal.valueOf(10000))
                .build();
        ProductResponse product2 = ProductResponse.builder()
                .id(UUID.randomUUID())
                .name("Producto 2")
                .price(BigDecimal.valueOf(15000))
                .build();

        when(productService.searchProducts(eq(query), any()))
                .thenReturn(List.of(product1, product2));

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/search")
                        .param("query", query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testGetAllProducts() throws Exception {
        // Arrange
        ProductResponse product1 = ProductResponse.builder()
                .id(UUID.randomUUID())
                .name("Producto 1")
                .price(BigDecimal.valueOf(10000))
                .stock(50)
                .build();
        ProductResponse product2 = ProductResponse.builder()
                .id(UUID.randomUUID())
                .name("Producto 2")
                .price(BigDecimal.valueOf(15000))
                .stock(30)
                .build();

        when(productService.getAllProducts()).thenReturn(List.of(product1, product2));

        // Act & Assert
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Producto 1"))
                .andExpect(jsonPath("$[1].name").value("Producto 2"));
    }

    @Test
    void testGetAllProducts_Empty() throws Exception {
        // Arrange
        when(productService.getAllProducts()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetProduct() throws Exception {
        // Arrange
        UUID productId = UUID.randomUUID();
        ProductResponse product = ProductResponse.builder()
                .id(productId)
                .name("Producto Test")
                .description("Descripción del producto")
                .price(BigDecimal.valueOf(20000))
                .stock(100)
                .category("Categoría")
                .sku("SKU-001")
                .build();

        when(productService.getProductById(productId)).thenReturn(product);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Producto Test"))
                .andExpect(jsonPath("$.description").value("Descripción del producto"))
                .andExpect(jsonPath("$.price").value(20000))
                .andExpect(jsonPath("$.stock").value(100))
                .andExpect(jsonPath("$.category").value("Categoría"))
                .andExpect(jsonPath("$.sku").value("SKU-001"));
    }

}

