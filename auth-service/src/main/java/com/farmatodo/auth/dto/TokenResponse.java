package com.farmatodo.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para respuesta de tokenización exitosa.
 * Contiene el token generado y su información de expiración.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {
    private UUID transactionId;
    private String token;
    private LocalDateTime expiresAt;
    private String message;
}

