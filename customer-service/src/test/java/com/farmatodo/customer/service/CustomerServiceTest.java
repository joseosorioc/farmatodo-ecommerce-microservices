package com.farmatodo.customer.service;

import com.farmatodo.customer.dto.CustomerRequest;
import com.farmatodo.customer.dto.CustomerResponse;
import com.farmatodo.customer.entity.Customer;
import com.farmatodo.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private CustomerRequest customerRequest;

    @BeforeEach
    void setUp() {
        customerRequest = new CustomerRequest();
        customerRequest.setFirstName("Juan");
        customerRequest.setLastName("Pérez");
        customerRequest.setEmail("juan@example.com");
        customerRequest.setPhone("+573001234567");
        customerRequest.setAddress("Calle 123");
        customerRequest.setCity("Bogotá");
    }

    @Test
    void testCreateCustomer_Success() {
        // Arrange
        when(customerRepository.existsByEmail(anyString())).thenReturn(false);
        when(customerRepository.existsByPhone(anyString())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer customer = invocation.getArgument(0);
            customer.setId(UUID.randomUUID());
            return customer;
        });

        // Act
        CustomerResponse response = customerService.createCustomer(customerRequest);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("Juan", response.getFirstName());
        assertEquals("juan@example.com", response.getEmail());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void testCreateCustomer_DuplicateEmail() {
        // Arrange
        when(customerRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            customerService.createCustomer(customerRequest);
        });
    }

    @Test
    void testGetCustomerById_Success() {
        // Arrange
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setEmail("juan@example.com");
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        // Act
        CustomerResponse response = customerService.getCustomerById(customerId);

        // Assert
        assertNotNull(response);
        assertEquals(customerId, response.getId());
        assertEquals("juan@example.com", response.getEmail());
    }
}

