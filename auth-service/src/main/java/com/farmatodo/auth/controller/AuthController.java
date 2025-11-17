package com.farmatodo.auth.controller;

import com.farmatodo.auth.dto.CreditCardRequest;
import com.farmatodo.auth.dto.ErrorResponse;
import com.farmatodo.auth.dto.TokenResponse;
import com.farmatodo.auth.service.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final TokenService tokenService;

    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "pong");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/tokens")
    public ResponseEntity<?> createToken(@Valid @RequestBody CreditCardRequest request) {
        try {
            TokenResponse response = tokenService.createToken(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .transactionId(UUID.randomUUID())
                    .timestamp(LocalDateTime.now())
                    .error("TOKEN_CREATION_FAILED")
                    .message(e.getMessage())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            log.error("Error inesperado al crear token", e);
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .transactionId(UUID.randomUUID())
                    .timestamp(LocalDateTime.now())
                    .error("INTERNAL_ERROR")
                    .message("Error interno del servidor")
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .transactionId(UUID.randomUUID())
                .timestamp(LocalDateTime.now())
                .error("VALIDATION_ERROR")
                .message("Errores de validación: " + errors.toString())
                .status(HttpStatus.BAD_REQUEST.value())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}

