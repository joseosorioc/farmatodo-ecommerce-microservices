package com.farmatodo.notification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private String customerEmail;
    private UUID orderId;
    private Double amount;

    @BeforeEach
    void setUp() {
        customerEmail = "cliente@example.com";
        orderId = UUID.randomUUID();
        amount = 150.50;
    }

    @Test
    void testSendPaymentSuccessEmail() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendPaymentSuccessEmail(customerEmail, orderId, amount);

        // Assert
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendPaymentSuccessEmail_ExceptionHandling() {
        // Arrange
        doThrow(new RuntimeException("Error de conexión SMTP"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // Act - No debe lanzar excepción, solo loguear el error
        emailService.sendPaymentSuccessEmail(customerEmail, orderId, amount);

        // Assert
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendPaymentFailureEmail() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendPaymentFailureEmail(customerEmail, orderId);

        // Assert
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendPaymentFailureEmail_ExceptionHandling() {
        // Arrange
        doThrow(new RuntimeException("Error de conexión SMTP"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // Act - No debe lanzar excepción, solo loguear el error
        emailService.sendPaymentFailureEmail(customerEmail, orderId);

        // Assert
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendPaymentSuccessEmail_MessageContent() {
        // Arrange
        doAnswer(invocation -> {
            SimpleMailMessage message = invocation.getArgument(0);
            org.junit.jupiter.api.Assertions.assertEquals(customerEmail, message.getTo()[0]);
            org.junit.jupiter.api.Assertions.assertTrue(message.getSubject().contains(orderId.toString()));
            org.junit.jupiter.api.Assertions.assertTrue(message.getText().contains(String.format("%.2f", amount)));
            return null;
        }).when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendPaymentSuccessEmail(customerEmail, orderId, amount);

        // Assert
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

}

