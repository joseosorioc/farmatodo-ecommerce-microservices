package com.farmatodo.notification.listener;

import com.farmatodo.notification.service.EmailService;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.pubsub.v1.PubsubMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener implements MessageReceiver {

    private final EmailService emailService;

    @Override
    public void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {
        try {
            String data = message.getData().toStringUtf8();
            log.info("Mensaje recibido para notificación: {}", data);

            // Parsear mensaje (simplificado)
            String status = extractStatus(data);
            String customerEmail = extractCustomerEmail(data);
            UUID orderId = extractOrderId(data);
            Double amount = extractAmount(data);

            if ("PAID".equals(status)) {
                emailService.sendPaymentSuccessEmail(customerEmail, orderId, amount);
            } else if ("FAILED".equals(status)) {
                emailService.sendPaymentFailureEmail(customerEmail, orderId);
            }

            consumer.ack();
        } catch (Exception e) {
            log.error("Error al procesar notificación", e);
            consumer.nack();
        }
    }

    private String extractStatus(String json) {
        String statusStr = json.substring(json.indexOf("\"status\":\"") + 10);
        return statusStr.substring(0, statusStr.indexOf("\""));
    }

    private String extractCustomerEmail(String json) {
        String emailStr = json.substring(json.indexOf("\"customerEmail\":\"") + 17);
        return emailStr.substring(0, emailStr.indexOf("\""));
    }

    private UUID extractOrderId(String json) {
        String orderIdStr = json.substring(json.indexOf("\"orderId\":\"") + 11);
        orderIdStr = orderIdStr.substring(0, orderIdStr.indexOf("\""));
        return UUID.fromString(orderIdStr);
    }

    private Double extractAmount(String json) {
        String amountStr = json.substring(json.indexOf("\"amount\":") + 9);
        amountStr = amountStr.substring(0, amountStr.indexOf(","));
        return Double.parseDouble(amountStr);
    }
}

