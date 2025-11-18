package com.farmatodo.cart.controller;

import com.farmatodo.cart.dto.CartItemRequest;
import com.farmatodo.cart.dto.CartItemResponse;
import com.farmatodo.cart.service.CartService;
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
import java.util.UUID;

/**
 * Controlador REST para gestión de carrito de compras.
 * Proporciona endpoints para agregar, consultar y eliminar items del carrito.
 */
@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cart", description = "API para gestión de carrito de compras")
public class CartController {

    private final CartService cartService;

    /**
     * Verifica el estado del servicio de carrito.
     * @return Respuesta con mensaje de estado
     */
    @Operation(summary = "Health check", description = "Verifica el estado del servicio")
    @ApiResponse(responseCode = "200", description = "Servicio activo")
    @GetMapping("/ping")
    public ResponseEntity<Object> ping() {
        return ResponseEntity.ok(java.util.Map.of("message", "pong"));
    }

    /**
     * Agrega un producto al carrito del cliente.
     * Si el producto ya existe, incrementa la cantidad.
     * @param customerId ID del cliente
     * @param request Datos del item a agregar
     * @return Item agregado al carrito
     */
    @Operation(summary = "Agregar item al carrito", description = "Agrega un producto al carrito del cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Item agregado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Error de validación")
    })
    @PostMapping("/{customerId}/items")
    public ResponseEntity<CartItemResponse> addItem(
            @Parameter(description = "ID del cliente") @PathVariable UUID customerId,
            @Valid @RequestBody CartItemRequest request) {
        CartItemResponse response = cartService.addItemToCart(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtiene todos los items del carrito de un cliente.
     * @param customerId ID del cliente
     * @return Lista de items del carrito
     */
    @Operation(summary = "Obtener items del carrito", description = "Retorna todos los items del carrito del cliente")
    @ApiResponse(responseCode = "200", description = "Lista de items del carrito")
    @GetMapping("/{customerId}/items")
    public ResponseEntity<List<CartItemResponse>> getCartItems(
            @Parameter(description = "ID del cliente") @PathVariable UUID customerId) {
        List<CartItemResponse> items = cartService.getCartItems(customerId);
        return ResponseEntity.ok(items);
    }

    /**
     * Elimina un producto específico del carrito del cliente.
     * @param customerId ID del cliente
     * @param productId ID del producto a eliminar
     * @return Respuesta vacía si se eliminó correctamente
     */
    @Operation(summary = "Eliminar item del carrito", description = "Elimina un producto específico del carrito")
    @ApiResponse(responseCode = "204", description = "Item eliminado exitosamente")
    @DeleteMapping("/{customerId}/items/{productId}")
    public ResponseEntity<Void> removeItem(
            @Parameter(description = "ID del cliente") @PathVariable UUID customerId,
            @Parameter(description = "ID del producto") @PathVariable UUID productId) {
        cartService.removeItemFromCart(customerId, productId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Elimina todos los items del carrito del cliente.
     * @param customerId ID del cliente
     * @return Respuesta vacía si se limpió correctamente
     */
    @Operation(summary = "Limpiar carrito", description = "Elimina todos los items del carrito del cliente")
    @ApiResponse(responseCode = "204", description = "Carrito limpiado exitosamente")
    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> clearCart(
            @Parameter(description = "ID del cliente") @PathVariable UUID customerId) {
        cartService.clearCart(customerId);
        return ResponseEntity.noContent().build();
    }
}

