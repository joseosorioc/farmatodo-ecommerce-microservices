package com.farmatodo.order.repository;

import com.farmatodo.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio para gestión de pedidos.
 * Proporciona métodos de consulta personalizados.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    /**
     * Busca todos los pedidos de un cliente.
     * @param customerId ID del cliente
     * @return Lista de pedidos del cliente
     */
    List<Order> findByCustomerId(UUID customerId);
}

