package com.farmatodo.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal del servicio de clientes.
 * Inicia la aplicación Spring Boot.
 */
@SpringBootApplication
public class CustomerServiceApplication {
    /**
     * Punto de entrada de la aplicación.
     * @param args Argumentos de línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceApplication.class, args);
    }
}

