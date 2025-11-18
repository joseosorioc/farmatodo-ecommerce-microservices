package com.farmatodo.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para respuestas de error del servicio.
 * Contiene información detallada sobre errores ocurridos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private UUID transactionId;
    private LocalDateTime timestamp;
    private String error;
    private String message;
    private int status;
}

