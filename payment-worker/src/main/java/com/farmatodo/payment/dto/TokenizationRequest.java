package com.farmatodo.payment.dto;

import lombok.Data;

/**
 * DTO para solicitud de tokenización de tarjeta.
 * Contiene los datos de la tarjeta a tokenizar.
 */
@Data
public class TokenizationRequest {
    private String cardNumber;
    private String cvv;
    private String expirationDate;
    private String cardHolderName;
    private String customerId;
}

