package com.farmatodo.product.repository;

import com.farmatodo.product.entity.ProductSearch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductSearchRepository extends JpaRepository<ProductSearch, UUID> {
}

