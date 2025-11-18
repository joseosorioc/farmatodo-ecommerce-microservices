package com.farmatodo.order.service;

import com.farmatodo.order.dto.OrderItemRequest;
import com.farmatodo.order.dto.OrderRequest;
import com.farmatodo.order.dto.OrderResponse;
import com.farmatodo.order.entity.Order;
import com.farmatodo.order.entity.OrderItem;
import com.farmatodo.order.repository.OrderItemRepository;
import com.farmatodo.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderService orderService;

    private UUID customerId;
    private OrderRequest orderRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orderService, "projectId", "local");
        customerId = UUID.randomUUID();

        OrderRequest.CreditCardData cardData = new OrderRequest.CreditCardData();
        cardData.setCardNumber("4111111111111111");
        cardData.setCvv("123");
        cardData.setExpirationDate("12/25");
        cardData.setCardHolderName("Juan Pérez");

        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(UUID.randomUUID());
        item1.setQuantity(2);

        OrderItemRequest item2 = new OrderItemRequest();
        item2.setProductId(UUID.randomUUID());
        item2.setQuantity(1);

        orderRequest = new OrderRequest();
        orderRequest.setCustomerId(customerId);
        orderRequest.setCardData(cardData);
        orderRequest.setDeliveryAddress("Calle 123, Bogotá");
        orderRequest.setItems(List.of(item1, item2));
    }

    @Test
    void testCreateOrder_Success() {
        // Arrange
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(UUID.randomUUID());
            return order;
        });
        when(orderItemRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderResponse response = orderService.createOrder(orderRequest);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals(customerId, response.getCustomerId());
        assertEquals("PENDING_PAYMENT", response.getStatus());
        assertEquals(2, response.getItems().size());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderItemRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testGetOrderById_Success() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder()
                .id(orderId)
                .customerId(customerId)
                .deliveryAddress("Calle 123")
                .totalAmount(new BigDecimal("200.00"))
                .status(Order.OrderStatus.PENDING_PAYMENT)
                .build();

        OrderItem item = OrderItem.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .productId(UUID.randomUUID())
                .quantity(2)
                .unitPrice(new BigDecimal("100.00"))
                .totalPrice(new BigDecimal("200.00"))
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(orderId)).thenReturn(List.of(item));

        // Act
        OrderResponse response = orderService.getOrderById(orderId);

        // Assert
        assertNotNull(response);
        assertEquals(orderId, response.getId());
        assertEquals(1, response.getItems().size());
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderItemRepository, times(1)).findByOrderId(orderId);
    }

    @Test
    void testGetOrderById_NotFound() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.getOrderById(orderId);
        });
        assertTrue(exception.getMessage().contains("no encontrado"));
    }

    @Test
    void testGetOrdersByCustomer() {
        // Arrange
        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();

        Order order1 = Order.builder()
                .id(orderId1)
                .customerId(customerId)
                .totalAmount(new BigDecimal("100.00"))
                .status(Order.OrderStatus.PAID)
                .build();

        Order order2 = Order.builder()
                .id(orderId2)
                .customerId(customerId)
                .totalAmount(new BigDecimal("200.00"))
                .status(Order.OrderStatus.PENDING_PAYMENT)
                .build();

        when(orderRepository.findByCustomerId(customerId)).thenReturn(List.of(order1, order2));
        when(orderItemRepository.findByOrderId(orderId1)).thenReturn(List.of());
        when(orderItemRepository.findByOrderId(orderId2)).thenReturn(List.of());

        // Act
        List<OrderResponse> orders = orderService.getOrdersByCustomer(customerId);

        // Assert
        assertNotNull(orders);
        assertEquals(2, orders.size());
        verify(orderRepository, times(1)).findByCustomerId(customerId);
    }

    @Test
    void testUpdateOrderStatus() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder()
                .id(orderId)
                .status(Order.OrderStatus.PENDING_PAYMENT)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        orderService.updateOrderStatus(orderId, Order.OrderStatus.PAID);

        // Assert
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testUpdateOrderStatusAndToken() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        String cardToken = "tok_1234567890";
        Order order = Order.builder()
                .id(orderId)
                .status(Order.OrderStatus.PENDING_PAYMENT)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        orderService.updateOrderStatusAndToken(orderId, Order.OrderStatus.PAID, cardToken);

        // Assert
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testUpdateOrderStatus_NotFound() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.updateOrderStatus(orderId, Order.OrderStatus.PAID);
        });
        assertTrue(exception.getMessage().contains("no encontrado"));
    }
}

