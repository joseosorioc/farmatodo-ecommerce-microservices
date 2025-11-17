package com.farmatodo.cart.repository;

import com.farmatodo.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {
    List<Cart> findByCustomerId(UUID customerId);
    Optional<Cart> findByCustomerIdAndProductId(UUID customerId, UUID productId);
    void deleteByCustomerId(UUID customerId);
}

