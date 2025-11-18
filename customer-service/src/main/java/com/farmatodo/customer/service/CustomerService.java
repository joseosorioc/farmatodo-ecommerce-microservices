package com.farmatodo.customer.service;

import com.farmatodo.customer.dto.CustomerRequest;
import com.farmatodo.customer.dto.CustomerResponse;
import com.farmatodo.customer.entity.Customer;
import com.farmatodo.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de clientes.
 * Maneja operaciones CRUD y validaciones de unicidad.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;

    /**
     * Crea un nuevo cliente en el sistema.
     * Valida que email y teléfono sean únicos.
     * @param request Datos del cliente a crear
     * @return Cliente creado
     */
    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        log.info("Creando cliente con email: {}", request.getEmail());

        // Validar que el email no exista
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado: " + request.getEmail());
        }

        // Validar que el teléfono no exista
        if (customerRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("El teléfono ya está registrado: " + request.getPhone());
        }

        Customer customer = Customer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .createdAt(LocalDateTime.now())
                .build();

        customer = customerRepository.save(customer);
        log.info("Cliente creado exitosamente con ID: {}", customer.getId());

        return mapToResponse(customer);
    }

    /**
     * Obtiene un cliente por su ID.
     * @param id ID del cliente
     * @return Cliente encontrado
     */
    public CustomerResponse getCustomerById(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
        return mapToResponse(customer);
    }

    /**
     * Obtiene todos los clientes registrados.
     * @return Lista de clientes
     */
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Actualiza la información de un cliente existente.
     * Valida que email y teléfono sean únicos si cambian.
     * @param id ID del cliente a actualizar
     * @param request Nuevos datos del cliente
     * @return Cliente actualizado
     */
    @Transactional
    public CustomerResponse updateCustomer(UUID id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));

        // Validar email único si cambió
        if (!customer.getEmail().equals(request.getEmail()) && 
            customerRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado: " + request.getEmail());
        }

        // Validar teléfono único si cambió
        if (!customer.getPhone().equals(request.getPhone()) && 
            customerRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("El teléfono ya está registrado: " + request.getPhone());
        }

        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setCity(request.getCity());
        customer.setState(request.getState());
        customer.setZipCode(request.getZipCode());
        customer.setUpdatedAt(LocalDateTime.now());

        customer = customerRepository.save(customer);
        return mapToResponse(customer);
    }

    /**
     * Convierte una entidad Customer a CustomerResponse.
     * @param customer Entidad del cliente
     * @return DTO de respuesta
     */
    private CustomerResponse mapToResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .city(customer.getCity())
                .state(customer.getState())
                .zipCode(customer.getZipCode())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}

