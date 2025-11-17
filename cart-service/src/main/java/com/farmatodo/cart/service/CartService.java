package com.farmatodo.cart.service;

import com.farmatodo.cart.dto.CartItemRequest;
import com.farmatodo.cart.dto.CartItemResponse;
import com.farmatodo.cart.entity.Cart;
import com.farmatodo.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;

    @Transactional
    public CartItemResponse addItemToCart(UUID customerId, CartItemRequest request) {
        log.info("Agregando producto {} al carrito del cliente {}", request.getProductId(), customerId);

        Optional<Cart> existingCart = cartRepository.findByCustomerIdAndProductId(customerId, request.getProductId());

        Cart cart;
        if (existingCart.isPresent()) {
            cart = existingCart.get();
            cart.setQuantity(cart.getQuantity() + request.getQuantity());
            cart.setUpdatedAt(LocalDateTime.now());
        } else {
            cart = Cart.builder()
                    .customerId(customerId)
                    .productId(request.getProductId())
                    .quantity(request.getQuantity())
                    .createdAt(LocalDateTime.now())
                    .build();
        }

        cart = cartRepository.save(cart);
        return mapToResponse(cart);
    }

    public List<CartItemResponse> getCartItems(UUID customerId) {
        return cartRepository.findByCustomerId(customerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeItemFromCart(UUID customerId, UUID productId) {
        cartRepository.findByCustomerIdAndProductId(customerId, productId)
                .ifPresent(cartRepository::delete);
    }

    @Transactional
    public void clearCart(UUID customerId) {
        cartRepository.deleteByCustomerId(customerId);
    }

    private CartItemResponse mapToResponse(Cart cart) {
        return CartItemResponse.builder()
                .id(cart.getId())
                .customerId(cart.getCustomerId())
                .productId(cart.getProductId())
                .quantity(cart.getQuantity())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }
}

