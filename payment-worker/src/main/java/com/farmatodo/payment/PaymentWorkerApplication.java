package com.farmatodo.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal del worker de procesamiento de pagos.
 * Inicia la aplicación Spring Boot.
 */
@SpringBootApplication
public class PaymentWorkerApplication {
    /**
     * Punto de entrada de la aplicación.
     * @param args Argumentos de línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(PaymentWorkerApplication.class, args);
    }
}

