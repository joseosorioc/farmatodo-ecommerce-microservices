package com.farmatodo.auth.repository;

import com.farmatodo.auth.entity.CreditCardToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreditCardTokenRepository extends JpaRepository<CreditCardToken, UUID> {
    Optional<CreditCardToken> findByToken(String token);
    boolean existsByToken(String token);
}

