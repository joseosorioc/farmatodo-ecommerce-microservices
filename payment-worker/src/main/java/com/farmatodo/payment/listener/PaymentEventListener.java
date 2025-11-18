package com.farmatodo.payment.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.farmatodo.payment.dto.CardData;
import com.farmatodo.payment.service.PaymentService;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.pubsub.v1.PubsubMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener implements MessageReceiver {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    @Value("${spring.cloud.gcp.pubsub.project-id:local}")
    private String projectId;

    @Override
    public void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {
        try {
            String data = message.getData().toStringUtf8();
            log.info("Mensaje recibido de Pub/Sub: {}", data);

            // Parsear mensaje JSON
            JsonNode jsonNode = objectMapper.readTree(data);
            
            UUID orderId = UUID.fromString(jsonNode.get("orderId").asText());
            UUID customerId = UUID.fromString(jsonNode.get("customerId").asText());
            Double amount = jsonNode.get("amount").asDouble();
            
            // Extraer datos de tarjeta
            JsonNode cardNode = jsonNode.get("card");
            CardData cardData = new CardData();
            cardData.setCardNumber(cardNode.get("cardNumber").asText());
            cardData.setCvv(cardNode.get("cvv").asText());
            cardData.setExpirationDate(cardNode.get("expirationDate").asText());
            cardData.setCardHolderName(cardNode.get("cardHolderName").asText());

            // Procesar pago (tokeniza y procesa)
            paymentService.processPayment(orderId, customerId, amount, cardData);

            consumer.ack();
        } catch (Exception e) {
            log.error("Error al procesar mensaje de pago", e);
            consumer.nack();
        }
    }
}

