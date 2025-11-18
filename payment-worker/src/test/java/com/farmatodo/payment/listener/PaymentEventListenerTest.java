package com.farmatodo.payment.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.farmatodo.payment.dto.CardData;
import com.farmatodo.payment.service.PaymentService;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.pubsub.v1.PubsubMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentEventListenerTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private AckReplyConsumer ackReplyConsumer;

    @InjectMocks
    private PaymentEventListener paymentEventListener;

    private ObjectMapper objectMapper;
    private UUID orderId;
    private UUID customerId;
    private Double amount;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        ReflectionTestUtils.setField(paymentEventListener, "objectMapper", objectMapper);
        ReflectionTestUtils.setField(paymentEventListener, "projectId", "local");

        orderId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        amount = 150.50;
    }

    @Test
    void testReceiveMessage_Success() throws Exception {
        // Arrange
        String jsonMessage = String.format(
            "{\"orderId\":\"%s\",\"customerId\":\"%s\",\"amount\":%.2f,\"card\":{\"cardNumber\":\"4111111111111111\",\"cvv\":\"123\",\"expirationDate\":\"12/25\",\"cardHolderName\":\"Juan Pérez\"}}",
            orderId, customerId, amount
        );
        PubsubMessage message = PubsubMessage.newBuilder()
                .setData(com.google.protobuf.ByteString.copyFromUtf8(jsonMessage))
                .build();

        doNothing().when(paymentService).processPayment(any(UUID.class), any(UUID.class), anyDouble(), any(CardData.class));
        doNothing().when(ackReplyConsumer).ack();

        // Act
        paymentEventListener.receiveMessage(message, ackReplyConsumer);

        // Assert
        verify(paymentService, times(1)).processPayment(
                eq(orderId),
                eq(customerId),
                eq(amount),
                any(CardData.class)
        );
        verify(ackReplyConsumer, times(1)).ack();
        verify(ackReplyConsumer, never()).nack();
    }

    @Test
    void testReceiveMessage_InvalidJson() {
        // Arrange
        String invalidJson = "invalid json";
        PubsubMessage message = PubsubMessage.newBuilder()
                .setData(com.google.protobuf.ByteString.copyFromUtf8(invalidJson))
                .build();

        doNothing().when(ackReplyConsumer).nack();

        // Act
        paymentEventListener.receiveMessage(message, ackReplyConsumer);

        // Assert
        verify(ackReplyConsumer, times(1)).nack();
        verify(ackReplyConsumer, never()).ack();
        verify(paymentService, never()).processPayment(any(), any(), anyDouble(), any());
    }

    @Test
    void testReceiveMessage_MissingFields() {
        // Arrange
        String incompleteJson = "{\"orderId\":\"" + orderId + "\"}";
        PubsubMessage message = PubsubMessage.newBuilder()
                .setData(com.google.protobuf.ByteString.copyFromUtf8(incompleteJson))
                .build();

        doNothing().when(ackReplyConsumer).nack();

        // Act
        paymentEventListener.receiveMessage(message, ackReplyConsumer);

        // Assert
        verify(ackReplyConsumer, times(1)).nack();
        verify(ackReplyConsumer, never()).ack();
        verify(paymentService, never()).processPayment(any(), any(), anyDouble(), any());
    }

    @Test
    void testReceiveMessage_ServiceException() throws Exception {
        // Arrange
        String jsonMessage = String.format(
            "{\"orderId\":\"%s\",\"customerId\":\"%s\",\"amount\":%.2f,\"card\":{\"cardNumber\":\"4111111111111111\",\"cvv\":\"123\",\"expirationDate\":\"12/25\",\"cardHolderName\":\"Juan Pérez\"}}",
            orderId, customerId, amount
        );
        PubsubMessage message = PubsubMessage.newBuilder()
                .setData(com.google.protobuf.ByteString.copyFromUtf8(jsonMessage))
                .build();

        doThrow(new RuntimeException("Error en procesamiento"))
                .when(paymentService).processPayment(any(UUID.class), any(UUID.class), anyDouble(), any(CardData.class));
        doNothing().when(ackReplyConsumer).nack();

        // Act
        paymentEventListener.receiveMessage(message, ackReplyConsumer);

        // Assert
        verify(paymentService, times(1)).processPayment(any(), any(), anyDouble(), any());
        verify(ackReplyConsumer, times(1)).nack();
        verify(ackReplyConsumer, never()).ack();
    }
}

