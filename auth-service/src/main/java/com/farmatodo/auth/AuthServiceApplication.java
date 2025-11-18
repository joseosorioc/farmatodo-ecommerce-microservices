package com.farmatodo.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal del servicio de autenticación.
 * Inicia la aplicación Spring Boot.
 */
@SpringBootApplication
public class AuthServiceApplication {
    /**
     * Punto de entrada de la aplicación.
     * @param args Argumentos de línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}

