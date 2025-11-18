package com.farmatodo.auth.repository;

import com.farmatodo.auth.entity.CreditCardToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para gestión de tokens de tarjetas de crédito.
 * Proporciona métodos de consulta personalizados.
 */
@Repository
public interface CreditCardTokenRepository extends JpaRepository<CreditCardToken, UUID> {
    /**
     * Busca un token por su valor.
     * @param token Valor del token a buscar
     * @return Token encontrado o vacío
     */
    Optional<CreditCardToken> findByToken(String token);
    
    /**
     * Verifica si existe un token con el valor especificado.
     * @param token Valor del token a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByToken(String token);
}

