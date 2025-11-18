package com.farmatodo.cart.repository;

import com.farmatodo.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para gestión de items del carrito.
 * Proporciona métodos de consulta personalizados.
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {
    /**
     * Busca todos los items del carrito de un cliente.
     * @param customerId ID del cliente
     * @return Lista de items del carrito
     */
    List<Cart> findByCustomerId(UUID customerId);
    
    /**
     * Busca un item específico del carrito por cliente y producto.
     * @param customerId ID del cliente
     * @param productId ID del producto
     * @return Item encontrado o vacío
     */
    Optional<Cart> findByCustomerIdAndProductId(UUID customerId, UUID productId);
    
    /**
     * Elimina todos los items del carrito de un cliente.
     * @param customerId ID del cliente
     */
    void deleteByCustomerId(UUID customerId);
}

