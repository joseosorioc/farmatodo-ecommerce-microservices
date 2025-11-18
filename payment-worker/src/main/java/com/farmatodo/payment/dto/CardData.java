package com.farmatodo.payment.dto;

import lombok.Data;

@Data
public class CardData {
    private String cardNumber;
    private String cvv;
    private String expirationDate;
    private String cardHolderName;
}


