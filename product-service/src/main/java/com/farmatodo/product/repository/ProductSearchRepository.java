package com.farmatodo.product.repository;

import com.farmatodo.product.entity.ProductSearch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repositorio para gestión de búsquedas de productos.
 * Extiende JpaRepository para operaciones CRUD básicas.
 */
@Repository
public interface ProductSearchRepository extends JpaRepository<ProductSearch, UUID> {
}

