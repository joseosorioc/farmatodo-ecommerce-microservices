package com.farmatodo.customer.repository;

import com.farmatodo.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByPhone(String phone);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}

