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

    @Test
    void testGetCustomerById_NotFound() {
        // Arrange
        UUID customerId = UUID.randomUUID();
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            customerService.getCustomerById(customerId);
        });
        assertTrue(exception.getMessage().contains("no encontrado"));
    }

    @Test
    void testGetAllCustomers() {
        // Arrange
        Customer customer1 = Customer.builder()
                .id(UUID.randomUUID())
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan@example.com")
                .phone("+573001234567")
                .address("Calle 123")
                .build();
        Customer customer2 = Customer.builder()
                .id(UUID.randomUUID())
                .firstName("María")
                .lastName("García")
                .email("maria@example.com")
                .phone("+573007654321")
                .address("Calle 456")
                .build();

        when(customerRepository.findAll()).thenReturn(java.util.List.of(customer1, customer2));

        // Act
        java.util.List<CustomerResponse> customers = customerService.getAllCustomers();

        // Assert
        assertNotNull(customers);
        assertEquals(2, customers.size());
        verify(customerRepository, times(1)).findAll();
    }

    @Test
    void testUpdateCustomer_Success() {
        // Arrange
        UUID customerId = UUID.randomUUID();
        Customer existingCustomer = Customer.builder()
                .id(customerId)
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan@example.com")
                .phone("+573001234567")
                .address("Calle 123")
                .build();

        CustomerRequest updateRequest = new CustomerRequest();
        updateRequest.setFirstName("Juan Carlos");
        updateRequest.setLastName("Pérez García");
        updateRequest.setEmail("juan@example.com"); // Mismo email
        updateRequest.setPhone("+573001234567"); // Mismo teléfono
        updateRequest.setAddress("Calle 456");
        updateRequest.setCity("Medellín");

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CustomerResponse response = customerService.updateCustomer(customerId, updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Juan Carlos", response.getFirstName());
        assertEquals("Calle 456", response.getAddress());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void testUpdateCustomer_NotFound() {
        // Arrange
        UUID customerId = UUID.randomUUID();
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            customerService.updateCustomer(customerId, customerRequest);
        });
        assertTrue(exception.getMessage().contains("no encontrado"));
    }

    @Test
    void testUpdateCustomer_DuplicateEmail() {
        // Arrange
        UUID customerId = UUID.randomUUID();
        Customer existingCustomer = Customer.builder()
                .id(customerId)
                .email("juan@example.com")
                .phone("+573001234567")
                .build();

        CustomerRequest updateRequest = new CustomerRequest();
        updateRequest.setEmail("nuevo@example.com"); // Email diferente
        updateRequest.setPhone("+573001234567");

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.existsByEmail("nuevo@example.com")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            customerService.updateCustomer(customerId, updateRequest);
        });
        assertTrue(exception.getMessage().contains("email ya está registrado"));
    }

    @Test
    void testCreateCustomer_DuplicatePhone() {
        // Arrange
        when(customerRepository.existsByEmail(anyString())).thenReturn(false);
        when(customerRepository.existsByPhone(anyString())).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            customerService.createCustomer(customerRequest);
        });
        assertTrue(exception.getMessage().contains("teléfono ya está registrado"));
    }
}

