package com.farmatodo.payment.service;

import com.farmatodo.payment.dto.CardData;
import com.farmatodo.payment.dto.TokenizationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PaymentService paymentService;

    private UUID orderId;
    private UUID customerId;
    private Double amount;
    private CardData cardData;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "rejectionProbability", 0.0);
        ReflectionTestUtils.setField(paymentService, "maxAttempts", 3);
        ReflectionTestUtils.setField(paymentService, "maxTokenizationAttempts", 3);
        ReflectionTestUtils.setField(paymentService, "maxUpdateAttempts", 3);
        ReflectionTestUtils.setField(paymentService, "orderServiceUrl", "http://localhost:8085");
        ReflectionTestUtils.setField(paymentService, "authServiceUrl", "http://localhost:8081");
        ReflectionTestUtils.setField(paymentService, "apiKey", "test-api-key");

        orderId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        amount = 150.50;

        cardData = new CardData();
        cardData.setCardNumber("4111111111111111");
        cardData.setCvv("123");
        cardData.setExpirationDate("12/25");
        cardData.setCardHolderName("Juan Pérez");
    }

    @Test
    void testProcessPayment_Success() {
        // Arrange
        TokenizationResponse tokenResponse = new TokenizationResponse();
        tokenResponse.setToken("tok_1234567890");
        tokenResponse.setTransactionId(UUID.randomUUID());
        tokenResponse.setExpiresAt(LocalDateTime.now().plusYears(5));

        ResponseEntity<TokenizationResponse> tokenEntity = new ResponseEntity<>(tokenResponse, HttpStatus.OK);
        ResponseEntity<Void> updateEntity = new ResponseEntity<>(HttpStatus.OK);

        when(restTemplate.exchange(
                eq("http://localhost:8081/api/v1/auth/tokens"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(TokenizationResponse.class)
        )).thenReturn(tokenEntity);

        when(restTemplate.exchange(
                contains("/api/v1/orders/"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(updateEntity);

        // Act
        paymentService.processPayment(orderId, customerId, amount, cardData);

        // Assert
        verify(restTemplate, atLeastOnce()).exchange(
                eq("http://localhost:8081/api/v1/auth/tokens"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(TokenizationResponse.class)
        );
        verify(restTemplate, atLeastOnce()).exchange(
                contains("/api/v1/orders/"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Void.class)
        );
    }

    @Test
    void testProcessPayment_TokenizationRejected() {
        // Arrange
        TokenizationResponse errorResponse = new TokenizationResponse();
        errorResponse.setError("TOKENIZATION_REJECTED");
        errorResponse.setMessage("Tokenización rechazada");

        ResponseEntity<TokenizationResponse> tokenEntity = new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        ResponseEntity<Void> updateEntity = new ResponseEntity<>(HttpStatus.OK);

        when(restTemplate.exchange(
                eq("http://localhost:8081/api/v1/auth/tokens"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(TokenizationResponse.class)
        )).thenReturn(tokenEntity);

        when(restTemplate.exchange(
                contains("/api/v1/orders/"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(updateEntity);

        // Act
        paymentService.processPayment(orderId, customerId, amount, cardData);

        // Assert
        verify(restTemplate, atLeastOnce()).exchange(
                eq("http://localhost:8081/api/v1/auth/tokens"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(TokenizationResponse.class)
        );
        verify(restTemplate, atLeastOnce()).exchange(
                contains("/api/v1/orders/"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Void.class)
        );
    }

    @Test
    void testProcessPayment_PaymentRejected() {
        // Arrange
        ReflectionTestUtils.setField(paymentService, "rejectionProbability", 1.0);

        TokenizationResponse tokenResponse = new TokenizationResponse();
        tokenResponse.setToken("tok_1234567890");
        tokenResponse.setTransactionId(UUID.randomUUID());

        ResponseEntity<TokenizationResponse> tokenEntity = new ResponseEntity<>(tokenResponse, HttpStatus.OK);
        ResponseEntity<Void> updateEntity = new ResponseEntity<>(HttpStatus.OK);

        when(restTemplate.exchange(
                eq("http://localhost:8081/api/v1/auth/tokens"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(TokenizationResponse.class)
        )).thenReturn(tokenEntity);

        when(restTemplate.exchange(
                contains("/api/v1/orders/"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(updateEntity);

        // Act
        paymentService.processPayment(orderId, customerId, amount, cardData);

        // Assert
        verify(restTemplate, atLeastOnce()).exchange(
                eq("http://localhost:8081/api/v1/auth/tokens"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(TokenizationResponse.class)
        );
        verify(restTemplate, atLeastOnce()).exchange(
                contains("/api/v1/orders/"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Void.class)
        );
    }

    @Test
    void testProcessPayment_TokenizationConnectionError() {
        // Arrange
        ResponseEntity<Void> updateEntity = new ResponseEntity<>(HttpStatus.OK);

        when(restTemplate.exchange(
                eq("http://localhost:8081/api/v1/auth/tokens"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(TokenizationResponse.class)
        )).thenThrow(new org.springframework.web.client.ResourceAccessException("Connection timeout"));

        when(restTemplate.exchange(
                contains("/api/v1/orders/"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(updateEntity);

        // Act
        paymentService.processPayment(orderId, customerId, amount, cardData);

        // Assert
        verify(restTemplate, atLeastOnce()).exchange(
                eq("http://localhost:8081/api/v1/auth/tokens"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(TokenizationResponse.class)
        );
    }

    @Test
    void testProcessPayment_UpdateOrderRetry() {
        // Arrange
        TokenizationResponse tokenResponse = new TokenizationResponse();
        tokenResponse.setToken("tok_1234567890");
        tokenResponse.setTransactionId(UUID.randomUUID());

        ResponseEntity<TokenizationResponse> tokenEntity = new ResponseEntity<>(tokenResponse, HttpStatus.OK);
        ResponseEntity<Void> updateEntity = new ResponseEntity<>(HttpStatus.OK);

        when(restTemplate.exchange(
                eq("http://localhost:8081/api/v1/auth/tokens"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(TokenizationResponse.class)
        )).thenReturn(tokenEntity);

        // Simular error de conexión en el primer intento, éxito en el segundo
        when(restTemplate.exchange(
                contains("/api/v1/orders/"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenThrow(new org.springframework.web.client.ResourceAccessException("Connection timeout"))
          .thenReturn(updateEntity);

        // Act
        paymentService.processPayment(orderId, customerId, amount, cardData);

        // Assert
        verify(restTemplate, atLeast(2)).exchange(
                contains("/api/v1/orders/"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Void.class)
        );
    }
}

