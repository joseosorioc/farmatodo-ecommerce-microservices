package com.farmatodo.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO para solicitud de creación o actualización de cliente.
 * Contiene los datos del cliente a procesar.
 */
@Data
public class CustomerRequest {
    @NotBlank(message = "El nombre es requerido")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String firstName;

    @NotBlank(message = "El apellido es requerido")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    private String lastName;

    @NotBlank(message = "El email es requerido")
    @Email(message = "El email debe tener un formato válido")
    private String email;

    @NotBlank(message = "El teléfono es requerido")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "El teléfono debe tener entre 10 y 15 dígitos")
    private String phone;

    @NotBlank(message = "La dirección es requerida")
    @Size(min = 5, max = 200, message = "La dirección debe tener entre 5 y 200 caracteres")
    private String address;

    @Size(max = 50, message = "La ciudad no puede exceder 50 caracteres")
    private String city;

    @Size(max = 50, message = "El estado no puede exceder 50 caracteres")
    private String state;

    @Size(max = 20, message = "El código postal no puede exceder 20 caracteres")
    private String zipCode;
}

