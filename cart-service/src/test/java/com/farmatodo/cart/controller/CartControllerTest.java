package com.farmatodo.cart.controller;

import com.farmatodo.cart.dto.CartItemRequest;
import com.farmatodo.cart.dto.CartItemResponse;
import com.farmatodo.cart.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testPing() throws Exception {
        mockMvc.perform(get("/api/v1/carts/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("pong"));
    }

    @Test
    void testAddItem() throws Exception {
        // Arrange
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        CartItemRequest request = new CartItemRequest();
        request.setProductId(productId);
        request.setQuantity(2);

        CartItemResponse response = CartItemResponse.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .productId(productId)
                .quantity(2)
                .createdAt(LocalDateTime.now())
                .build();

        when(cartService.addItemToCart(eq(customerId), any(CartItemRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/carts/{customerId}/items", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(productId.toString()))
                .andExpect(jsonPath("$.quantity").value(2));
    }

    @Test
    void testGetCartItems() throws Exception {
        // Arrange
        UUID customerId = UUID.randomUUID();
        CartItemResponse item1 = CartItemResponse.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .productId(UUID.randomUUID())
                .quantity(2)
                .build();
        CartItemResponse item2 = CartItemResponse.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .productId(UUID.randomUUID())
                .quantity(1)
                .build();

        when(cartService.getCartItems(customerId))
                .thenReturn(List.of(item1, item2));

        // Act & Assert
        mockMvc.perform(get("/api/v1/carts/{customerId}/items", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testRemoveItem() throws Exception {
        // Arrange
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        doNothing().when(cartService).removeItemFromCart(customerId, productId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/carts/{customerId}/items/{productId}", customerId, productId))
                .andExpect(status().isNoContent());

        verify(cartService, times(1)).removeItemFromCart(customerId, productId);
    }

    @Test
    void testClearCart() throws Exception {
        // Arrange
        UUID customerId = UUID.randomUUID();
        doNothing().when(cartService).clearCart(customerId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/carts/{customerId}", customerId))
                .andExpect(status().isNoContent());

        verify(cartService, times(1)).clearCart(customerId);
    }

    @Test
    void testAddItem_ValidationError() throws Exception {
        // Arrange
        UUID customerId = UUID.randomUUID();
        CartItemRequest request = new CartItemRequest();
        request.setQuantity(0); // Cantidad inválida

        // Act & Assert
        mockMvc.perform(post("/api/v1/carts/{customerId}/items", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}

