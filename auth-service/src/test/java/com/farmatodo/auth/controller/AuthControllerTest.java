package com.farmatodo.auth.controller;

import com.farmatodo.auth.dto.CreditCardRequest;
import com.farmatodo.auth.dto.TokenResponse;
import com.farmatodo.auth.service.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {"app.api-key=test-api-key"})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TokenService tokenService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testPing() throws Exception {
        mockMvc.perform(get("/api/v1/auth/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("pong"));
    }

    @Test
    void testCreateToken_Success() throws Exception {
        CreditCardRequest request = new CreditCardRequest();
        request.setCardNumber("4111111111111111");
        request.setCvv("123");
        request.setExpirationDate("12/25");
        request.setCardHolderName("Juan Pérez");
        request.setCustomerId(UUID.randomUUID().toString());

        TokenResponse response = TokenResponse.builder()
                .transactionId(UUID.randomUUID())
                .token("tok_1234567890abcdef")
                .expiresAt(LocalDateTime.now().plusYears(5))
                .message("Token creado exitosamente")
                .build();

        when(tokenService.createToken(any(CreditCardRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.message").value("Token creado exitosamente"));
    }

    @Test
    void testCreateToken_ValidationError() throws Exception {
        CreditCardRequest request = new CreditCardRequest();
        request.setCardNumber("123");
        request.setCvv("12");

        mockMvc.perform(post("/api/v1/auth/tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void testCreateToken_RuntimeException() throws Exception {
        CreditCardRequest request = new CreditCardRequest();
        request.setCardNumber("4111111111111111");
        request.setCvv("123");
        request.setExpirationDate("12/25");
        request.setCardHolderName("Juan Pérez");
        request.setCustomerId(UUID.randomUUID().toString());

        when(tokenService.createToken(any(CreditCardRequest.class)))
                .thenThrow(new RuntimeException("La tokenización fue rechazada"));

        mockMvc.perform(post("/api/v1/auth/tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("TOKEN_CREATION_FAILED"))
                .andExpect(jsonPath("$.message").value("La tokenización fue rechazada"));
    }
}

