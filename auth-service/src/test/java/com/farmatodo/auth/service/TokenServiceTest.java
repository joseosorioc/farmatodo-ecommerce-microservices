package com.farmatodo.auth.service;

import com.farmatodo.auth.dto.CreditCardRequest;
import com.farmatodo.auth.dto.TokenResponse;
import com.farmatodo.auth.entity.CreditCardToken;
import com.farmatodo.auth.repository.CreditCardTokenRepository;
import com.farmatodo.auth.repository.TransactionLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private CreditCardTokenRepository tokenRepository;

    @Mock
    private TransactionLogRepository transactionLogRepository;

    @InjectMocks
    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tokenService, "rejectionProbability", 0.0);
    }

    @Test
    void testCreateToken_Success() {
        // Arrange
        CreditCardRequest request = new CreditCardRequest();
        request.setCardNumber("4111111111111111");
        request.setCvv("123");
        request.setExpirationDate("12/25");
        request.setCardHolderName("Juan Pérez");
        request.setCustomerId(UUID.randomUUID().toString());

        when(tokenRepository.existsByToken(anyString())).thenReturn(false);
        when(tokenRepository.save(any(CreditCardToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TokenResponse response = tokenService.createToken(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertNotNull(response.getTransactionId());
        verify(tokenRepository, times(1)).save(any(CreditCardToken.class));
    }

    @Test
    void testGetTokenByTokenValue() {
        // Arrange
        String token = "tok_1234567890";
        CreditCardToken creditCardToken = new CreditCardToken();
        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(creditCardToken));

        // Act
        Optional<CreditCardToken> result = tokenService.getTokenByTokenValue(token);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(creditCardToken, result.get());
    }

    @Test
    void testGetTokenByTokenValue_NotFound() {
        // Arrange
        String token = "tok_nonexistent";
        when(tokenRepository.findByToken(token)).thenReturn(Optional.empty());

        // Act
        Optional<CreditCardToken> result = tokenService.getTokenByTokenValue(token);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testCreateToken_Rejection() {
        // Arrange
        ReflectionTestUtils.setField(tokenService, "rejectionProbability", 1.0);
        CreditCardRequest request = new CreditCardRequest();
        request.setCardNumber("4111111111111111");
        request.setCvv("123");
        request.setExpirationDate("12/25");
        request.setCardHolderName("Juan Pérez");
        request.setCustomerId(UUID.randomUUID().toString());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tokenService.createToken(request);
        });

        assertTrue(exception.getMessage().contains("rechazada"));
        verify(tokenRepository, never()).save(any(CreditCardToken.class));
    }

    @Test
    void testCreateToken_MasksCardNumberCorrectly() {
        // Arrange
        CreditCardRequest request = new CreditCardRequest();
        request.setCardNumber("4111111111111111");
        request.setCvv("123");
        request.setExpirationDate("12/25");
        request.setCardHolderName("Juan Pérez");
        request.setCustomerId(UUID.randomUUID().toString());

        when(tokenRepository.existsByToken(anyString())).thenReturn(false);
        when(tokenRepository.save(any(CreditCardToken.class))).thenAnswer(invocation -> {
            CreditCardToken token = invocation.getArgument(0);
            assertTrue(token.getMaskedCardNumber().contains("1111"));
            assertTrue(token.getMaskedCardNumber().startsWith("****"));
            assertEquals("1111", token.getLastFourDigits());
            return token;
        });

        // Act
        tokenService.createToken(request);

        // Assert
        verify(tokenRepository, times(1)).save(any(CreditCardToken.class));
    }
}

