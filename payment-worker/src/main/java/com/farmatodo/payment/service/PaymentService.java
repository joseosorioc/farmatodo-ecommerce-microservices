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

/**
 * Servicio para procesamiento de pagos.
 * Maneja tokenización de tarjetas y procesamiento de pagos con reintentos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final RestTemplate restTemplate;

    @Value("${app.payment-rejection-probability:0.2}")
    private double rejectionProbability;

    @Value("${app.max-payment-attempts:3}")
    private int maxAttempts;

    @Value("${app.max-tokenization-attempts:3}")
    private int maxTokenizationAttempts = 3;

    @Value("${app.max-update-attempts:3}")
    private int maxUpdateAttempts = 3;

    @Value("${ORDER_SERVICE_URL:http://localhost:8085}")
    private String orderServiceUrl;

    @Value("${AUTH_SERVICE_URL:http://localhost:8081}")
    private String authServiceUrl;

    @Value("${API_KEY:test-api-key-12345}")
    private String apiKey;

    /**
     * Procesa un pago: tokeniza la tarjeta y procesa el pago.
     * Maneja reintentos y actualiza el estado del pedido.
     * @param orderId ID del pedido
     * @param customerId ID del cliente
     * @param amount Monto del pago
     * @param cardData Datos de la tarjeta
     */
    public void processPayment(UUID orderId, UUID customerId, Double amount, CardData cardData) {
        log.info("Procesando pago para orden: {}, monto: {}", orderId, amount);

        // Paso 1: Tokenizar la tarjeta llamando a auth-service (con reintentos)
        TokenizationResponse tokenizationResponse = tokenizeCardWithRetries(cardData, customerId, orderId);

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

    private TokenizationResponse tokenizeCardWithRetries(CardData cardData, UUID customerId, UUID orderId) {
        log.info("Iniciando tokenización de tarjeta para cliente: {} (orden: {})", customerId, orderId);
        
        for (int attempt = 1; attempt <= maxTokenizationAttempts; attempt++) {
            log.info("Intento de tokenización {} de {} para orden: {}", attempt, maxTokenizationAttempts, orderId);
            
            TokenizationResponse response = tokenizeCard(cardData, customerId);
            
            // Si la tokenización fue exitosa, retornar
            if (response != null && response.getToken() != null && response.getError() == null) {
                log.info("Tokenización exitosa en intento {} para orden: {}", attempt, orderId);
                return response;
            }
            
            // Si es el último intento, retornar el error
            if (attempt == maxTokenizationAttempts) {
                log.error("Todos los intentos de tokenización fallaron para orden: {}", orderId);
                return response;
            }
            
            // Esperar antes del siguiente intento (backoff exponencial)
            int waitTime = (int) Math.pow(2, attempt) * 1000; // 2s, 4s, 8s...
            log.warn("Tokenización falló en intento {} para orden: {}. Reintentando en {}ms...", 
                    attempt, orderId, waitTime);
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Interrupción durante espera de reintento de tokenización", e);
                break;
            }
        }
        
        // Si llegamos aquí, todos los intentos fallaron
        TokenizationResponse errorResponse = new TokenizationResponse();
        errorResponse.setError("TOKENIZATION_ERROR");
        errorResponse.setMessage("Todos los intentos de tokenización fallaron");
        return errorResponse;
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

            String url = authServiceUrl + "/api/v1/auth/tokens";
            log.debug("Llamando a auth-service: {}", url);
            
            HttpEntity<TokenizationRequest> httpEntity = new HttpEntity<>(request, headers);
            ResponseEntity<TokenizationResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                httpEntity,
                TokenizationResponse.class
            );

            log.debug("Respuesta recibida de auth-service. Status: {}", response.getStatusCode());
            
            if (response.getStatusCode().is2xxSuccessful()) {
                TokenizationResponse tokenResponse = response.getBody();
                if (tokenResponse != null && tokenResponse.getToken() != null) {
                    log.debug("Token generado exitosamente: {}", tokenResponse.getToken());
                } else {
                    log.warn("Respuesta exitosa pero sin token. Response: {}", tokenResponse);
                }
                return tokenResponse;
            } else {
                log.warn("Error al tokenizar tarjeta. Status: {}", response.getStatusCode());
                TokenizationResponse errorResponse = new TokenizationResponse();
                errorResponse.setError("TOKENIZATION_FAILED");
                errorResponse.setMessage("Error al tokenizar tarjeta. Status: " + response.getStatusCode());
                return errorResponse;
            }
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // Errores de conexión (timeout, broken pipe, etc.)
            log.warn("Error de conexión al tokenizar tarjeta: {}", e.getMessage());
            TokenizationResponse errorResponse = new TokenizationResponse();
            errorResponse.setError("TOKENIZATION_CONNECTION_ERROR");
            errorResponse.setMessage("Error de conexión: " + e.getMessage());
            return errorResponse;
        } catch (Exception e) {
            log.warn("Error al tokenizar tarjeta: {}", e.getMessage());
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
        updateOrderStatusAndTokenWithRetries(orderId, status, cardToken);
    }

    private void updateOrderStatusAndTokenWithRetries(UUID orderId, String status, String cardToken) {
        for (int attempt = 1; attempt <= maxUpdateAttempts; attempt++) {
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
                log.info("Estado y token actualizados exitosamente para orden: {} (intento {})", orderId, attempt);
                return; // Éxito, salir
            } catch (org.springframework.web.client.ResourceAccessException e) {
                // Error de conexión, reintentar
                if (attempt == maxUpdateAttempts) {
                    log.error("Error al actualizar estado y token de orden: {} después de {} intentos", orderId, maxUpdateAttempts, e);
                } else {
                    int waitTime = attempt * 1000; // 1s, 2s, 3s...
                    log.warn("Error de conexión al actualizar estado y token de orden: {} (intento {}). Reintentando en {}ms...", 
                            orderId, attempt, waitTime);
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("Interrupción durante espera de reintento de actualización", ie);
                        return;
                    }
                }
            } catch (Exception e) {
                log.error("Error al actualizar estado y token de orden: {} (intento {})", orderId, attempt, e);
                return; // Otro tipo de error, no reintentar
            }
        }
    }

    private void handlePaymentRejection(UUID orderId, UUID customerId, Double amount, String reason) {
        updateOrderStatus(orderId, "PAYMENT_REJECTED");
        publishPaymentRejectedEvent(orderId, customerId, amount, reason);
    }

    private void updateOrderStatus(UUID orderId, String status) {
        updateOrderStatusWithRetries(orderId, status);
    }

    private void updateOrderStatusWithRetries(UUID orderId, String status) {
        for (int attempt = 1; attempt <= maxUpdateAttempts; attempt++) {
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
                log.info("Estado actualizado exitosamente para orden: {} (intento {})", orderId, attempt);
                return; // Éxito, salir
            } catch (org.springframework.web.client.ResourceAccessException e) {
                // Error de conexión, reintentar
                if (attempt == maxUpdateAttempts) {
                    log.error("Error al actualizar estado de orden: {} después de {} intentos", orderId, maxUpdateAttempts, e);
                } else {
                    int waitTime = attempt * 1000; // 1s, 2s, 3s...
                    log.warn("Error de conexión al actualizar estado de orden: {} (intento {}). Reintentando en {}ms...", 
                            orderId, attempt, waitTime);
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("Interrupción durante espera de reintento de actualización", ie);
                        return;
                    }
                }
            } catch (Exception e) {
                log.error("Error al actualizar estado de orden: {} (intento {})", orderId, attempt, e);
                return; // Otro tipo de error, no reintentar
            }
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

