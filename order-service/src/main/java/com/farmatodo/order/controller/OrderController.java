package com.farmatodo.order.controller;

import com.farmatodo.order.dto.OrderRequest;
import com.farmatodo.order.dto.OrderResponse;
import com.farmatodo.order.entity.Order;
import com.farmatodo.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/ping")
    public ResponseEntity<Object> ping() {
        return ResponseEntity.ok(java.util.Map.of("message", "pong"));
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID id) {
        OrderResponse response = orderService.getOrderById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomer(@PathVariable UUID customerId) {
        List<OrderResponse> orders = orderService.getOrdersByCustomer(customerId);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateOrderStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> request) {
        Order.OrderStatus status = Order.OrderStatus.valueOf(request.get("status"));
        String cardToken = request.get("cardToken");
        
        if (cardToken != null && !cardToken.isEmpty()) {
            orderService.updateOrderStatusAndToken(id, status, cardToken);
        } else {
            orderService.updateOrderStatus(id, status);
        }
        return ResponseEntity.ok().build();
    }
}

