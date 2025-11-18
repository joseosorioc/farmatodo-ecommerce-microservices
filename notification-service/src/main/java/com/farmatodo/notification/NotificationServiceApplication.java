package com.farmatodo.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal del servicio de notificaciones.
 * Inicia la aplicación Spring Boot.
 */
@SpringBootApplication
public class NotificationServiceApplication {
    /**
     * Punto de entrada de la aplicación.
     * @param args Argumentos de línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}

