package com.farmatodo.auth.service;

import com.farmatodo.auth.dto.CreditCardRequest;
import com.farmatodo.auth.dto.TokenResponse;
import com.farmatodo.auth.entity.CreditCardToken;
import com.farmatodo.auth.entity.TransactionLog;
import com.farmatodo.auth.repository.CreditCardTokenRepository;
import com.farmatodo.auth.repository.TransactionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/**
 * Servicio para la creación y gestión de tokens de tarjetas de crédito.
 * Maneja la tokenización, enmascaramiento y registro de transacciones.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final CreditCardTokenRepository tokenRepository;
    private final TransactionLogRepository transactionLogRepository;

    @Value("${app.token-rejection-probability:0.1}")
    private double rejectionProbability;

    /**
     * Crea un token único para una tarjeta de crédito.
     * Enmascara el número de tarjeta y registra la transacción.
     * @param request Datos de la tarjeta a tokenizar
     * @return Respuesta con el token generado
     */
    @Transactional
    public TokenResponse createToken(CreditCardRequest request) {
        UUID transactionId = UUID.randomUUID();
        
        try {
            log.info("Iniciando tokenización para cliente: {}", request.getCustomerId());

            // Verificar probabilidad de rechazo
            if (shouldReject()) {
                log.warn("Tokenización rechazada por probabilidad configurada. TransactionId: {}", transactionId);
                saveTransactionLog(transactionId, "TOKEN_CREATION", request, null, "REJECTED", 
                    "Token rechazado por probabilidad configurada");
                throw new RuntimeException("La tokenización fue rechazada. Por favor intente nuevamente.");
            }

            // Generar token único
            String token = generateUniqueToken();

            // Enmascarar número de tarjeta (solo guardar últimos 4 dígitos)
            String cardNumber = request.getCardNumber();
            String lastFourDigits = cardNumber.length() >= 4 
                ? cardNumber.substring(cardNumber.length() - 4) 
                : cardNumber;
            String maskedCardNumber = "****-****-****-" + lastFourDigits;

            // Crear entidad
            CreditCardToken creditCardToken = CreditCardToken.builder()
                    .token(token)
                    .lastFourDigits(lastFourDigits)
                    .maskedCardNumber(maskedCardNumber)
                    .expirationDate(request.getExpirationDate())
                    .cardHolderName(request.getCardHolderName())
                    .customerId(UUID.fromString(request.getCustomerId()))
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusYears(5))
                    .transactionId(transactionId)
                    .build();

            // Guardar en base de datos
            tokenRepository.save(creditCardToken);

            TokenResponse response = TokenResponse.builder()
                    .transactionId(transactionId)
                    .token(token)
                    .expiresAt(creditCardToken.getExpiresAt())
                    .message("Token creado exitosamente")
                    .build();

            saveTransactionLog(transactionId, "TOKEN_CREATION", request, response, "SUCCESS", null);
            
            log.info("Token creado exitosamente. Token: {}, TransactionId: {}", token, transactionId);
            return response;

        } catch (Exception e) {
            log.error("Error al crear token. TransactionId: {}", transactionId, e);
            saveTransactionLog(transactionId, "TOKEN_CREATION", request, null, "ERROR", e.getMessage());
            throw e;
        }
    }

    /**
     * Busca un token por su valor.
     * @param token Valor del token a buscar
     * @return Token encontrado o vacío
     */
    public Optional<CreditCardToken> getTokenByTokenValue(String token) {
        return tokenRepository.findByToken(token);
    }

    /**
     * Genera un token único que no existe en la base de datos.
     * @return Token único generado
     */
    private String generateUniqueToken() {
        String token;
        do {
            token = "tok_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        } while (tokenRepository.existsByToken(token));
        return token;
    }

    /**
     * Determina si una tokenización debe ser rechazada según probabilidad configurada.
     * @return true si debe rechazarse, false en caso contrario
     */
    private boolean shouldReject() {
        Random random = new Random();
        return random.nextDouble() < rejectionProbability;
    }

    /**
     * Guarda un registro de transacción en la base de datos.
     * @param transactionId ID único de la transacción
     * @param operation Tipo de operación realizada
     * @param request Datos de la petición
     * @param response Datos de la respuesta
     * @param status Estado de la transacción
     * @param errorMessage Mensaje de error si existe
     */
    private void saveTransactionLog(UUID transactionId, String operation, Object request, 
                                   Object response, String status, String errorMessage) {
        try {
            TransactionLog log = TransactionLog.builder()
                    .transactionId(transactionId)
                    .serviceName("auth-service")
                    .operation(operation)
                    .requestData(request != null ? request.toString() : null)
                    .responseData(response != null ? response.toString() : null)
                    .status(status)
                    .errorMessage(errorMessage)
                    .timestamp(LocalDateTime.now())
                    .build();
            transactionLogRepository.save(log);
        } catch (Exception e) {
            log.error("Error al guardar log de transacción", e);
        }
    }
}

