package com.farmatodo.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal del servicio de productos.
 * Inicia la aplicación Spring Boot.
 */
@SpringBootApplication
public class ProductServiceApplication {
    /**
     * Punto de entrada de la aplicación.
     * @param args Argumentos de línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}

