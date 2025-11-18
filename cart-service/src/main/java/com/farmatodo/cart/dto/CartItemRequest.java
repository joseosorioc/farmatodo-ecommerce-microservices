package com.farmatodo.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/**
 * DTO para solicitud de agregar item al carrito.
 * Contiene el ID del producto y la cantidad.
 */
@Data
public class CartItemRequest {
    @NotNull(message = "El ID del producto es requerido")
    private UUID productId;

    @NotNull(message = "La cantidad es requerida")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer quantity;
}

