package com.farmatodo.product.repository;

import com.farmatodo.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio para gestión de productos.
 * Proporciona métodos de búsqueda personalizados.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    /**
     * Busca productos por término de búsqueda en nombre, descripción o categoría.
     * Solo retorna productos con stock mayor al mínimo.
     * @param query Término de búsqueda
     * @param minStock Stock mínimo requerido
     * @return Lista de productos encontrados
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.category) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "p.stock > :minStock")
    List<Product> searchProducts(@Param("query") String query, @Param("minStock") Integer minStock);
}

