package com.farmatodo.cart.service;

import com.farmatodo.cart.dto.CartItemRequest;
import com.farmatodo.cart.dto.CartItemResponse;
import com.farmatodo.cart.entity.Cart;
import com.farmatodo.cart.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private CartService cartService;

    private UUID customerId;
    private UUID productId;
    private CartItemRequest request;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        productId = UUID.randomUUID();
        request = new CartItemRequest();
        request.setProductId(productId);
        request.setQuantity(2);
    }

    @Test
    void testAddItemToCart_NewItem() {
        // Arrange
        when(cartRepository.findByCustomerIdAndProductId(customerId, productId))
                .thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart cart = invocation.getArgument(0);
            cart.setId(UUID.randomUUID());
            return cart;
        });

        // Act
        CartItemResponse response = cartService.addItemToCart(customerId, request);

        // Assert
        assertNotNull(response);
        assertEquals(productId, response.getProductId());
        assertEquals(2, response.getQuantity());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void testAddItemToCart_ExistingItem() {
        // Arrange
        Cart existingCart = new Cart();
        existingCart.setId(UUID.randomUUID());
        existingCart.setCustomerId(customerId);
        existingCart.setProductId(productId);
        existingCart.setQuantity(3);

        when(cartRepository.findByCustomerIdAndProductId(customerId, productId))
                .thenReturn(Optional.of(existingCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CartItemResponse response = cartService.addItemToCart(customerId, request);

        // Assert
        assertNotNull(response);
        assertEquals(5, response.getQuantity()); // 3 + 2
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void testGetCartItems() {
        // Arrange
        Cart cart1 = Cart.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .productId(productId)
                .quantity(2)
                .build();
        Cart cart2 = Cart.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .productId(UUID.randomUUID())
                .quantity(1)
                .build();

        when(cartRepository.findByCustomerId(customerId))
                .thenReturn(java.util.List.of(cart1, cart2));

        // Act
        java.util.List<CartItemResponse> items = cartService.getCartItems(customerId);

        // Assert
        assertNotNull(items);
        assertEquals(2, items.size());
        verify(cartRepository, times(1)).findByCustomerId(customerId);
    }

    @Test
    void testGetCartItems_EmptyCart() {
        // Arrange
        when(cartRepository.findByCustomerId(customerId))
                .thenReturn(java.util.List.of());

        // Act
        java.util.List<CartItemResponse> items = cartService.getCartItems(customerId);

        // Assert
        assertNotNull(items);
        assertTrue(items.isEmpty());
        verify(cartRepository, times(1)).findByCustomerId(customerId);
    }

    @Test
    void testRemoveItemFromCart() {
        // Arrange
        Cart cart = Cart.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .productId(productId)
                .quantity(2)
                .build();

        when(cartRepository.findByCustomerIdAndProductId(customerId, productId))
                .thenReturn(Optional.of(cart));
        doNothing().when(cartRepository).delete(cart);

        // Act
        cartService.removeItemFromCart(customerId, productId);

        // Assert
        verify(cartRepository, times(1)).findByCustomerIdAndProductId(customerId, productId);
        verify(cartRepository, times(1)).delete(cart);
    }

    @Test
    void testRemoveItemFromCart_ItemNotFound() {
        // Arrange
        when(cartRepository.findByCustomerIdAndProductId(customerId, productId))
                .thenReturn(Optional.empty());

        // Act
        cartService.removeItemFromCart(customerId, productId);

        // Assert
        verify(cartRepository, times(1)).findByCustomerIdAndProductId(customerId, productId);
        verify(cartRepository, never()).delete(any(Cart.class));
    }

    @Test
    void testClearCart() {
        // Arrange
        doNothing().when(cartRepository).deleteByCustomerId(customerId);

        // Act
        cartService.clearCart(customerId);

        // Assert
        verify(cartRepository, times(1)).deleteByCustomerId(customerId);
    }
}

