package com.farmatodo.payment.config;

import com.farmatodo.payment.listener.PaymentEventListener;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Configuración de Pub/Sub para suscripción a eventos.
 * Inicia el subscriber para escuchar eventos de pedidos.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class PubSubConfig {

    private final PaymentEventListener paymentEventListener;

    @Value("${spring.cloud.gcp.pubsub.project-id:local}")
    private String projectId;

    private Subscriber subscriber;

    /**
     * Inicializa el subscriber de Pub/Sub al arrancar la aplicación.
     * Configura la suscripción al topic de pedidos creados.
     */
    @PostConstruct
    public void init() {
        // Verificar si hay emulador configurado o si es GCP real
        String emulatorHost = System.getenv("PUBSUB_EMULATOR_HOST");
        boolean usePubSub = emulatorHost != null || !"local".equals(projectId);
        
        if (usePubSub) {
            try {
                ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(projectId, "order-created-sub");
                subscriber = Subscriber.newBuilder(subscriptionName, paymentEventListener).build();
                subscriber.startAsync().awaitRunning();
                log.info("Subscriber iniciado para: {} (Emulador: {})", subscriptionName, emulatorHost != null ? "Sí" : "No");
            } catch (Exception e) {
                log.error("Error al iniciar subscriber", e);
            }
        } else {
            log.info("Pub/Sub no configurado (modo local)");
        }
    }

    /**
     * Detiene el subscriber al cerrar la aplicación.
     */
    @PreDestroy
    public void destroy() {
        if (subscriber != null) {
            subscriber.stopAsync();
        }
    }
}

