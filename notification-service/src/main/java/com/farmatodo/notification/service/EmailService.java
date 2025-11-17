package com.farmatodo.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendPaymentSuccessEmail(String customerEmail, UUID orderId, Double amount) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(customerEmail);
            message.setSubject("Pago Exitoso - Pedido #" + orderId);
            message.setText(String.format(
                "Estimado cliente,\n\n" +
                "Su pago ha sido procesado exitosamente.\n\n" +
                "Detalles del pedido:\n" +
                "- Número de pedido: %s\n" +
                "- Monto: $%.2f\n\n" +
                "Gracias por su compra.\n\n" +
                "Atentamente,\n" +
                "Farmatodo",
                orderId, amount
            ));
            mailSender.send(message);
            log.info("Email de éxito enviado a: {}", customerEmail);
        } catch (Exception e) {
            log.error("Error al enviar email de éxito", e);
        }
    }

    public void sendPaymentFailureEmail(String customerEmail, UUID orderId) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(customerEmail);
            message.setSubject("Pago Fallido - Pedido #" + orderId);
            message.setText(String.format(
                "Estimado cliente,\n\n" +
                "Lamentamos informarle que su pago no pudo ser procesado después de varios intentos.\n\n" +
                "Detalles del pedido:\n" +
                "- Número de pedido: %s\n\n" +
                "Por favor, verifique los datos de su tarjeta e intente nuevamente.\n\n" +
                "Si el problema persiste, contacte a nuestro servicio al cliente.\n\n" +
                "Atentamente,\n" +
                "Farmatodo",
                orderId
            ));
            mailSender.send(message);
            log.info("Email de fallo enviado a: {}", customerEmail);
        } catch (Exception e) {
            log.error("Error al enviar email de fallo", e);
        }
    }
}

