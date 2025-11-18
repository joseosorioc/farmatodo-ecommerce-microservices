package com.farmatodo.payment.dto;

import lombok.Data;

/**
 * DTO para datos de tarjeta de crédito.
 * Contiene información de la tarjeta para procesamiento.
 */
@Data
public class CardData {
    private String cardNumber;
    private String cvv;
    private String expirationDate;
    private String cardHolderName;
}


