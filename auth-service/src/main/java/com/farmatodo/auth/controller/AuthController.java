package com.farmatodo.auth.controller;

import com.farmatodo.auth.dto.CreditCardRequest;
import com.farmatodo.auth.dto.ErrorResponse;
import com.farmatodo.auth.dto.TokenResponse;
import com.farmatodo.auth.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

/**
 * Controlador REST para autenticación y tokenización de tarjetas de crédito.
 * Proporciona endpoints para crear tokens seguros y verificar el estado del servicio.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Auth", description = "API de autenticación y tokenización de tarjetas")
public class AuthController {

    private final TokenService tokenService;

    /**
     * Verifica el estado del servicio de autenticación.
     * @return Respuesta con mensaje de estado
     */
    @Operation(summary = "Health check", description = "Verifica el estado del servicio")
    @ApiResponse(responseCode = "200", description = "Servicio activo")
    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "pong");
        return ResponseEntity.ok(response);
    }

    /**
     * Crea un token seguro para una tarjeta de crédito.
     * Valida los datos de la tarjeta y genera un token único.
     * @param request Datos de la tarjeta de crédito a tokenizar
     * @return Respuesta con el token generado o error
     */
    @Operation(
            summary = "Crear token de tarjeta",
            description = "Tokeniza una tarjeta de crédito y retorna un token seguro"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Token creado exitosamente",
                    content = @Content(schema = @Schema(implementation = TokenResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error en la validación o tokenización rechazada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @SecurityRequirement(name = "ApiKeyAuth")
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

    /**
     * Maneja excepciones de validación de datos de entrada.
     * @param ex Excepción de validación capturada
     * @return Respuesta de error con detalles de validación
     */
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

