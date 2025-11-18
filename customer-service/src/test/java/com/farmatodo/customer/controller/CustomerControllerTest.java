package com.farmatodo.customer.controller;

import com.farmatodo.customer.dto.CustomerRequest;
import com.farmatodo.customer.dto.CustomerResponse;
import com.farmatodo.customer.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testPing() throws Exception {
        mockMvc.perform(get("/api/v1/customers/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("pong"));
    }

    @Test
    void testCreateCustomer() throws Exception {
        // Arrange
        CustomerRequest request = new CustomerRequest();
        request.setFirstName("Juan");
        request.setLastName("Pérez");
        request.setEmail("juan@example.com");
        request.setPhone("+573001234567");
        request.setAddress("Calle 123");
        request.setCity("Bogotá");

        CustomerResponse response = CustomerResponse.builder()
                .id(UUID.randomUUID())
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan@example.com")
                .phone("+573001234567")
                .address("Calle 123")
                .city("Bogotá")
                .createdAt(LocalDateTime.now())
                .build();

        when(customerService.createCustomer(any(CustomerRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Juan"))
                .andExpect(jsonPath("$.email").value("juan@example.com"));
    }

    @Test
    void testGetCustomer() throws Exception {
        // Arrange
        UUID customerId = UUID.randomUUID();
        CustomerResponse response = CustomerResponse.builder()
                .id(customerId)
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan@example.com")
                .phone("+573001234567")
                .address("Calle 123")
                .build();

        when(customerService.getCustomerById(customerId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customerId.toString()))
                .andExpect(jsonPath("$.firstName").value("Juan"));
    }

    @Test
    void testGetAllCustomers() throws Exception {
        // Arrange
        CustomerResponse customer1 = CustomerResponse.builder()
                .id(UUID.randomUUID())
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan@example.com")
                .build();
        CustomerResponse customer2 = CustomerResponse.builder()
                .id(UUID.randomUUID())
                .firstName("María")
                .lastName("García")
                .email("maria@example.com")
                .build();

        when(customerService.getAllCustomers()).thenReturn(List.of(customer1, customer2));

        // Act & Assert
        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testUpdateCustomer() throws Exception {
        // Arrange
        UUID customerId = UUID.randomUUID();
        CustomerRequest request = new CustomerRequest();
        request.setFirstName("Juan Carlos");
        request.setLastName("Pérez García");
        request.setEmail("juan@example.com");
        request.setPhone("+573001234567");
        request.setAddress("Calle 456");
        request.setCity("Medellín");

        CustomerResponse response = CustomerResponse.builder()
                .id(customerId)
                .firstName("Juan Carlos")
                .lastName("Pérez García")
                .email("juan@example.com")
                .phone("+573001234567")
                .address("Calle 456")
                .city("Medellín")
                .updatedAt(LocalDateTime.now())
                .build();

        when(customerService.updateCustomer(eq(customerId), any(CustomerRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/customers/{id}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Juan Carlos"))
                .andExpect(jsonPath("$.address").value("Calle 456"));
    }

    @Test
    void testCreateCustomer_ValidationError() throws Exception {
        // Arrange
        CustomerRequest request = new CustomerRequest();
        request.setFirstName("J"); // Nombre muy corto
        request.setEmail("invalid-email"); // Email inválido

        // Act & Assert
        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}

