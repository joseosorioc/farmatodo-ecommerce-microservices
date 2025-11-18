package com.farmatodo.order.repository;

import com.farmatodo.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio para gestión de items de pedidos.
 * Proporciona métodos de consulta personalizados.
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
    /**
     * Busca todos los items de un pedido.
     * @param orderId ID del pedido
     * @return Lista de items del pedido
     */
    List<OrderItem> findByOrderId(UUID orderId);
}

