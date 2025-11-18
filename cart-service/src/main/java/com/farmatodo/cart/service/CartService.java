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

/**
 * Servicio para gestión de carrito de compras.
 * Maneja operaciones de agregar, consultar y eliminar items del carrito.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;

    /**
     * Agrega un item al carrito del cliente.
     * Si el producto ya existe, incrementa la cantidad.
     * @param customerId ID del cliente
     * @param request Datos del item a agregar
     * @return Item agregado al carrito
     */
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

    /**
     * Obtiene todos los items del carrito de un cliente.
     * @param customerId ID del cliente
     * @return Lista de items del carrito
     */
    public List<CartItemResponse> getCartItems(UUID customerId) {
        return cartRepository.findByCustomerId(customerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Elimina un producto específico del carrito del cliente.
     * @param customerId ID del cliente
     * @param productId ID del producto a eliminar
     */
    @Transactional
    public void removeItemFromCart(UUID customerId, UUID productId) {
        cartRepository.findByCustomerIdAndProductId(customerId, productId)
                .ifPresent(cartRepository::delete);
    }

    /**
     * Elimina todos los items del carrito del cliente.
     * @param customerId ID del cliente
     */
    @Transactional
    public void clearCart(UUID customerId) {
        cartRepository.deleteByCustomerId(customerId);
    }

    /**
     * Convierte una entidad Cart a CartItemResponse.
     * @param cart Entidad del carrito
     * @return DTO de respuesta
     */
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

