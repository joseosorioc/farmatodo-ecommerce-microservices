package com.farmatodo.auth.repository;

import com.farmatodo.auth.entity.TransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repositorio para gestión de logs de transacciones.
 * Extiende JpaRepository para operaciones CRUD básicas.
 */
@Repository
public interface TransactionLogRepository extends JpaRepository<TransactionLog, UUID> {
}

