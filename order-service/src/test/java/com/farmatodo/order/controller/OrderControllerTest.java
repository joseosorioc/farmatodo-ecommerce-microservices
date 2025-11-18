package com.farmatodo.order.controller;

import com.farmatodo.order.dto.OrderItemRequest;
import com.farmatodo.order.dto.OrderRequest;
import com.farmatodo.order.dto.OrderResponse;
import com.farmatodo.order.entity.Order;
import com.farmatodo.order.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testPing() throws Exception {
        mockMvc.perform(get("/api/v1/orders/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("pong"));
    }

    @Test
    void testCreateOrder() throws Exception {
        // Arrange
        UUID customerId = UUID.randomUUID();
        OrderRequest.CreditCardData cardData = new OrderRequest.CreditCardData();
        cardData.setCardNumber("4111111111111111");
        cardData.setCvv("123");
        cardData.setExpirationDate("12/25");
        cardData.setCardHolderName("Juan Pérez");

        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(UUID.randomUUID());
        item.setQuantity(2);

        OrderRequest request = new OrderRequest();
        request.setCustomerId(customerId);
        request.setCardData(cardData);
        request.setDeliveryAddress("Calle 123, Bogotá");
        request.setItems(List.of(item));

        OrderResponse response = OrderResponse.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .deliveryAddress("Calle 123, Bogotá")
                .totalAmount(new BigDecimal("200.00"))
                .status("PENDING_PAYMENT")
                .createdAt(LocalDateTime.now())
                .transactionId(UUID.randomUUID())
                .items(List.of())
                .build();

        when(orderService.createOrder(any(OrderRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"));
    }

    @Test
    void testGetOrder() throws Exception {
        // Arrange
        UUID orderId = UUID.randomUUID();
        OrderResponse response = OrderResponse.builder()
                .id(orderId)
                .customerId(UUID.randomUUID())
                .totalAmount(new BigDecimal("200.00"))
                .status("PAID")
                .build();

        when(orderService.getOrderById(orderId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void testGetOrdersByCustomer() throws Exception {
        // Arrange
        UUID customerId = UUID.randomUUID();
        OrderResponse order1 = OrderResponse.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .status("PAID")
                .build();
        OrderResponse order2 = OrderResponse.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .status("PENDING_PAYMENT")
                .build();

        when(orderService.getOrdersByCustomer(customerId)).thenReturn(List.of(order1, order2));

        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/customer/{customerId}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testUpdateOrderStatus() throws Exception {
        // Arrange
        UUID orderId = UUID.randomUUID();
        Map<String, String> request = Map.of("status", "PAID");
        doNothing().when(orderService).updateOrderStatus(eq(orderId), eq(Order.OrderStatus.PAID));

        // Act & Assert
        mockMvc.perform(put("/api/v1/orders/{id}/status", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(orderService, times(1)).updateOrderStatus(eq(orderId), eq(Order.OrderStatus.PAID));
    }

    @Test
    void testUpdateOrderStatusWithToken() throws Exception {
        // Arrange
        UUID orderId = UUID.randomUUID();
        Map<String, String> request = Map.of(
                "status", "PAID",
                "cardToken", "tok_1234567890"
        );
        doNothing().when(orderService).updateOrderStatusAndToken(
                eq(orderId), eq(Order.OrderStatus.PAID), eq("tok_1234567890"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/orders/{id}/status", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(orderService, times(1)).updateOrderStatusAndToken(
                eq(orderId), eq(Order.OrderStatus.PAID), eq("tok_1234567890"));
    }

    @Test
    void testCreateOrder_ValidationError() throws Exception {
        // Arrange
        OrderRequest request = new OrderRequest();
        // Faltan campos requeridos

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}

