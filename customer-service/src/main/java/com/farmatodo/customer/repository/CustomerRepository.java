package com.farmatodo.customer.repository;

import com.farmatodo.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para gestión de clientes.
 * Proporciona métodos de consulta personalizados.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    /**
     * Busca un cliente por su email.
     * @param email Email del cliente
     * @return Cliente encontrado o vacío
     */
    Optional<Customer> findByEmail(String email);
    
    /**
     * Busca un cliente por su teléfono.
     * @param phone Teléfono del cliente
     * @return Cliente encontrado o vacío
     */
    Optional<Customer> findByPhone(String phone);
    
    /**
     * Verifica si existe un cliente con el email especificado.
     * @param email Email a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByEmail(String email);
    
    /**
     * Verifica si existe un cliente con el teléfono especificado.
     * @param phone Teléfono a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByPhone(String phone);
}

