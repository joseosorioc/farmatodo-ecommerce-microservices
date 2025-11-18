package com.farmatodo.notification.listener;

import com.farmatodo.notification.service.EmailService;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.pubsub.v1.PubsubMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private AckReplyConsumer ackReplyConsumer;

    @InjectMocks
    private NotificationEventListener notificationEventListener;

    private UUID orderId;
    private String customerEmail;
    private Double amount;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        customerEmail = "cliente@example.com";
        amount = 150.50;
    }

    @Test
    void testReceiveMessage_PaymentSuccess() {
        // Arrange - El amount debe tener coma después porque el extractAmount busca hasta la coma
        String jsonMessage = String.format(
            "{\"status\":\"PAID\",\"customerEmail\":\"%s\",\"orderId\":\"%s\",\"amount\":%.2f,\"extra\":\"value\"}",
            customerEmail, orderId, amount
        );
        PubsubMessage message = PubsubMessage.newBuilder()
                .setData(com.google.protobuf.ByteString.copyFromUtf8(jsonMessage))
                .build();

        doNothing().when(emailService).sendPaymentSuccessEmail(anyString(), any(UUID.class), anyDouble());
        doNothing().when(ackReplyConsumer).ack();

        // Act
        notificationEventListener.receiveMessage(message, ackReplyConsumer);

        // Assert
        verify(emailService, times(1))
                .sendPaymentSuccessEmail(eq(customerEmail), eq(orderId), eq(amount));
        verify(ackReplyConsumer, times(1)).ack();
        verify(ackReplyConsumer, never()).nack();
    }

    @Test
    void testReceiveMessage_PaymentFailed() {
        // Arrange - El amount debe tener coma después porque el extractAmount busca hasta la coma
        String jsonMessage = String.format(
            "{\"status\":\"FAILED\",\"customerEmail\":\"%s\",\"orderId\":\"%s\",\"amount\":%.2f,\"extra\":\"value\"}",
            customerEmail, orderId, amount
        );
        PubsubMessage message = PubsubMessage.newBuilder()
                .setData(com.google.protobuf.ByteString.copyFromUtf8(jsonMessage))
                .build();

        doNothing().when(emailService).sendPaymentFailureEmail(anyString(), any(UUID.class));
        doNothing().when(ackReplyConsumer).ack();

        // Act
        notificationEventListener.receiveMessage(message, ackReplyConsumer);

        // Assert
        verify(emailService, times(1))
                .sendPaymentFailureEmail(eq(customerEmail), eq(orderId));
        verify(ackReplyConsumer, times(1)).ack();
        verify(ackReplyConsumer, never()).nack();
    }

    @Test
    void testReceiveMessage_ExceptionHandling() {
        // Arrange
        String invalidJson = "invalid json";
        PubsubMessage message = PubsubMessage.newBuilder()
                .setData(com.google.protobuf.ByteString.copyFromUtf8(invalidJson))
                .build();

        doNothing().when(ackReplyConsumer).nack();

        // Act
        notificationEventListener.receiveMessage(message, ackReplyConsumer);

        // Assert
        verify(ackReplyConsumer, times(1)).nack();
        verify(ackReplyConsumer, never()).ack();
        verify(emailService, never()).sendPaymentSuccessEmail(anyString(), any(UUID.class), anyDouble());
        verify(emailService, never()).sendPaymentFailureEmail(anyString(), any(UUID.class));
    }
}

