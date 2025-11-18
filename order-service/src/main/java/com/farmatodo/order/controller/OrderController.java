package com.farmatodo.order.controller;

import com.farmatodo.order.dto.OrderRequest;
import com.farmatodo.order.dto.OrderResponse;
import com.farmatodo.order.entity.Order;
import com.farmatodo.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Order", description = "API para gestión de pedidos")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Health check", description = "Verifica el estado del servicio")
    @ApiResponse(responseCode = "200", description = "Servicio activo")
    @GetMapping("/ping")
    public ResponseEntity<Object> ping() {
        return ResponseEntity.ok(java.util.Map.of("message", "pong"));
    }

    @Operation(summary = "Crear pedido", description = "Crea un nuevo pedido con los items y datos de tarjeta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pedido creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Error de validación")
    })
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Obtener pedido por ID", description = "Retorna la información de un pedido específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(
            @Parameter(description = "ID del pedido") @PathVariable UUID id) {
        OrderResponse response = orderService.getOrderById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener pedidos por cliente", description = "Retorna todos los pedidos de un cliente")
    @ApiResponse(responseCode = "200", description = "Lista de pedidos del cliente")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomer(
            @Parameter(description = "ID del cliente") @PathVariable UUID customerId) {
        List<OrderResponse> orders = orderService.getOrdersByCustomer(customerId);
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Actualizar estado del pedido", description = "Actualiza el estado y opcionalmente el token de tarjeta del pedido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateOrderStatus(
            @Parameter(description = "ID del pedido") @PathVariable UUID id,
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

