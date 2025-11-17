package com.farmatodo.payment.service;

import com.farmatodo.payment.dto.CardData;
import com.farmatodo.payment.dto.TokenizationRequest;
import com.farmatodo.payment.dto.TokenizationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final RestTemplate restTemplate;

    @Value("${app.payment-rejection-probability:0.2}")
    private double rejectionProbability;

    @Value("${app.max-payment-attempts:3}")
    private int maxAttempts;

    @Value("${ORDER_SERVICE_URL:http://localhost:8085}")
    private String orderServiceUrl;

    @Value("${AUTH_SERVICE_URL:http://localhost:8081}")
    private String authServiceUrl;

    @Value("${API_KEY:test-api-key-12345}")
    private String apiKey;

    public void processPayment(UUID orderId, UUID customerId, Double amount, CardData cardData) {
        log.info("Procesando pago para orden: {}, monto: {}", orderId, amount);

        // Paso 1: Tokenizar la tarjeta llamando a auth-service
        TokenizationResponse tokenizationResponse = tokenizeCard(cardData, customerId);

        if (tokenizationResponse == null || tokenizationResponse.getToken() == null || tokenizationResponse.getError() != null) {
            // Tokenización rechazada
            log.warn("Tokenización rechazada para orden: {}", orderId);
            String reason = tokenizationResponse != null && tokenizationResponse.getMessage() != null 
                ? tokenizationResponse.getMessage() 
                : "Error en tokenización";
            handlePaymentRejection(orderId, customerId, amount, reason);
            return;
        }

        // Paso 2: Tokenización exitosa, ahora procesar el pago
        String token = tokenizationResponse.getToken();
        log.info("Token generado exitosamente: {} para orden: {}", token, orderId);

        // Procesar pago con reintentos
        boolean paymentSuccess = processPaymentWithRetries(orderId, amount);

        if (paymentSuccess) {
            // Paso 3: Actualizar orden con token y estado PAID
            updateOrderStatusAndToken(orderId, "PAID", token);
            
            // Publicar evento payment-approved
            publishPaymentApprovedEvent(orderId, customerId, amount, token);
        } else {
            // Todos los intentos fallaron
            updateOrderStatusAndToken(orderId, "PAYMENT_REJECTED", token);
            
            // Publicar evento payment-rejected
            publishPaymentRejectedEvent(orderId, customerId, amount, "Todos los intentos de pago fallaron");
        }
    }

    private TokenizationResponse tokenizeCard(CardData cardData, UUID customerId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-API-Key", apiKey);
            headers.set("Content-Type", "application/json");

            TokenizationRequest request = new TokenizationRequest();
            request.setCardNumber(cardData.getCardNumber());
            request.setCvv(cardData.getCvv());
            request.setExpirationDate(cardData.getExpirationDate());
            request.setCardHolderName(cardData.getCardHolderName());
            request.setCustomerId(customerId.toString());

            HttpEntity<TokenizationRequest> httpEntity = new HttpEntity<>(request, headers);
            ResponseEntity<TokenizationResponse> response = restTemplate.exchange(
                authServiceUrl + "/api/v1/tokens",
                HttpMethod.POST,
                httpEntity,
                TokenizationResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                log.error("Error al tokenizar tarjeta. Status: {}", response.getStatusCode());
                TokenizationResponse errorResponse = new TokenizationResponse();
                errorResponse.setError("TOKENIZATION_FAILED");
                errorResponse.setMessage("Error al tokenizar tarjeta");
                return errorResponse;
            }
        } catch (Exception e) {
            log.error("Error al tokenizar tarjeta", e);
            TokenizationResponse errorResponse = new TokenizationResponse();
            errorResponse.setError("TOKENIZATION_ERROR");
            errorResponse.setMessage(e.getMessage());
            return errorResponse;
        }
    }

    private boolean processPaymentWithRetries(UUID orderId, Double amount) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            log.info("Intento de pago {} de {} para orden: {}", attempt, maxAttempts, orderId);

            if (shouldRejectPayment()) {
                log.warn("Pago rechazado en intento {} para orden: {}", attempt, orderId);
                
                if (attempt == maxAttempts) {
                    log.error("Todos los intentos de pago fallaron para orden: {}", orderId);
                    return false;
                }
                
                // Esperar un poco antes del siguiente intento
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                log.info("Pago aprobado en intento {} para orden: {}", attempt, orderId);
                return true;
            }
        }

        return false;
    }

    private boolean shouldRejectPayment() {
        Random random = new Random();
        return random.nextDouble() < rejectionProbability;
    }

    private void updateOrderStatusAndToken(UUID orderId, String status, String cardToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            Map<String, String> body = new HashMap<>();
            body.put("status", status);
            body.put("cardToken", cardToken);
            
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
            restTemplate.exchange(
                orderServiceUrl + "/api/v1/orders/" + orderId + "/status",
                HttpMethod.PUT,
                request,
                Void.class
            );
        } catch (Exception e) {
            log.error("Error al actualizar estado y token de orden: {}", orderId, e);
        }
    }

    private void handlePaymentRejection(UUID orderId, UUID customerId, Double amount, String reason) {
        updateOrderStatus(orderId, "PAYMENT_REJECTED");
        publishPaymentRejectedEvent(orderId, customerId, amount, reason);
    }

    private void updateOrderStatus(UUID orderId, String status) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            Map<String, String> body = new HashMap<>();
            body.put("status", status);
            
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
            restTemplate.exchange(
                orderServiceUrl + "/api/v1/orders/" + orderId + "/status",
                HttpMethod.PUT,
                request,
                Void.class
            );
        } catch (Exception e) {
            log.error("Error al actualizar estado de orden: {}", orderId, e);
        }
    }

    private void publishPaymentApprovedEvent(UUID orderId, UUID customerId, Double amount, String token) {
        // TODO: Implementar publicación a Pub/Sub topic payment-approved
        log.info("Evento payment-approved publicado para orden: {}", orderId);
    }

    private void publishPaymentRejectedEvent(UUID orderId, UUID customerId, Double amount, String reason) {
        // TODO: Implementar publicación a Pub/Sub topic payment-rejected
        log.info("Evento payment-rejected publicado para orden: {}, razón: {}", orderId, reason);
    }
}

