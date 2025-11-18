package com.farmatodo.payment.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para respuesta de tokenización.
 * Contiene el token generado o información de error.
 */
@Data
public class TokenizationResponse {
    private UUID transactionId;
    private String token;
    private LocalDateTime expiresAt;
    private String message;
    private String error;
    private Integer status;
}

