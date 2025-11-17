package com.farmatodo.cart.controller;

import com.farmatodo.cart.dto.CartItemRequest;
import com.farmatodo.cart.dto.CartItemResponse;
import com.farmatodo.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    @GetMapping("/ping")
    public ResponseEntity<Object> ping() {
        return ResponseEntity.ok(java.util.Map.of("message", "pong"));
    }

    @PostMapping("/{customerId}/items")
    public ResponseEntity<CartItemResponse> addItem(
            @PathVariable UUID customerId,
            @Valid @RequestBody CartItemRequest request) {
        CartItemResponse response = cartService.addItemToCart(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{customerId}/items")
    public ResponseEntity<List<CartItemResponse>> getCartItems(@PathVariable UUID customerId) {
        List<CartItemResponse> items = cartService.getCartItems(customerId);
        return ResponseEntity.ok(items);
    }

    @DeleteMapping("/{customerId}/items/{productId}")
    public ResponseEntity<Void> removeItem(
            @PathVariable UUID customerId,
            @PathVariable UUID productId) {
        cartService.removeItemFromCart(customerId, productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> clearCart(@PathVariable UUID customerId) {
        cartService.clearCart(customerId);
        return ResponseEntity.noContent().build();
    }
}

