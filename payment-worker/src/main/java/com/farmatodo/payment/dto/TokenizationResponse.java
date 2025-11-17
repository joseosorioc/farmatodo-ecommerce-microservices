package com.farmatodo.payment.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TokenizationResponse {
    private UUID transactionId;
    private String token;
    private LocalDateTime expiresAt;
    private String message;
    private String error;
    private Integer status;
}

