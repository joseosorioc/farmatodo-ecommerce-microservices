package com.farmatodo.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * DTO para solicitud de creación de pedido.
 * Contiene datos del cliente, tarjeta, dirección e items.
 */
@Data
public class OrderRequest {
    @NotNull(message = "El ID del cliente es requerido")
    private UUID customerId;

    @NotNull(message = "Los datos de tarjeta son requeridos")
    @Valid
    private CreditCardData cardData;

    @NotBlank(message = "La dirección de entrega es requerida")
    private String deliveryAddress;

    @NotNull(message = "Los items del pedido son requeridos")
    private List<OrderItemRequest> items;

    @Data
    public static class CreditCardData {
        @NotBlank(message = "El número de tarjeta es requerido")
        private String cardNumber;

        @NotBlank(message = "El CVV es requerido")
        private String cvv;

        @NotBlank(message = "La fecha de expiración es requerida")
        private String expirationDate;

        @NotBlank(message = "El nombre del titular es requerido")
        private String cardHolderName;
    }
}

