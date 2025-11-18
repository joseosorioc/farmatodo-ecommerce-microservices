package com.farmatodo.order.service;

import com.farmatodo.order.dto.OrderRequest;
import com.farmatodo.order.dto.OrderResponse;
import com.farmatodo.order.entity.Order;
import com.farmatodo.order.entity.OrderItem;
import com.farmatodo.order.repository.OrderItemRepository;
import com.farmatodo.order.repository.OrderRepository;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Value("${spring.cloud.gcp.pubsub.project-id:local}")
    private String projectId;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        UUID transactionId = UUID.randomUUID();
        log.info("Creando pedido para cliente: {}, TransactionId: {}", request.getCustomerId(), transactionId);

        // Calcular total
        BigDecimal totalAmount = request.getItems().stream()
                .map(item -> BigDecimal.valueOf(100.0).multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Crear pedido (cardToken se llenará después de tokenizar en payment-worker)
        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .cardToken(null) // Se llenará después de tokenizar
                .deliveryAddress(request.getDeliveryAddress())
                .totalAmount(totalAmount)
                .status(Order.OrderStatus.PENDING_PAYMENT)
                .createdAt(LocalDateTime.now())
                .transactionId(transactionId)
                .paymentAttempts(0)
                .build();

        order = orderRepository.save(order);

        // Crear items del pedido
        Order finalOrder = order;
        List<OrderItem> orderItems = request.getItems().stream()
                .map(item -> OrderItem.builder()
                        .orderId(finalOrder.getId())
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .unitPrice(BigDecimal.valueOf(100.0))
                        .totalPrice(BigDecimal.valueOf(100.0).multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .collect(Collectors.toList());

        orderItemRepository.saveAll(orderItems);

        // Publicar evento de pago con datos de tarjeta para que payment-worker los tokenice
        publishPaymentEvent(order, request.getCardData());

        return mapToResponse(order, orderItems);
    }

    public OrderResponse getOrderById(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));
        List<OrderItem> items = orderItemRepository.findByOrderId(id);
        return mapToResponse(order, items);
    }

    public List<OrderResponse> getOrdersByCustomer(UUID customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
                .map(order -> {
                    List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
                    return mapToResponse(order, items);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateOrderStatus(UUID orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Transactional
    public void updateOrderStatusAndToken(UUID orderId, Order.OrderStatus status, String cardToken) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        order.setStatus(status);
        order.setCardToken(cardToken);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    private void publishPaymentEvent(Order order, OrderRequest.CreditCardData cardData) {
        try {
            // Verificar si hay emulador configurado o si es GCP real
            String emulatorHost = System.getenv("PUBSUB_EMULATOR_HOST");
            boolean usePubSub = emulatorHost != null || !"local".equals(projectId);
            
            if (usePubSub) {
                TopicName topicName = TopicName.of(projectId, "order-created");
                Publisher publisher = Publisher.newBuilder(topicName).build();
                
                // Publicar evento con datos de tarjeta para que payment-worker los tokenice
                String message = String.format(
                    "{\"orderId\":\"%s\",\"customerId\":\"%s\",\"amount\":%s,\"card\":{\"cardNumber\":\"%s\",\"cvv\":\"%s\",\"expirationDate\":\"%s\",\"cardHolderName\":\"%s\"}}",
                    order.getId(), 
                    order.getCustomerId(), 
                    order.getTotalAmount(),
                    cardData.getCardNumber(),
                    cardData.getCvv(),
                    cardData.getExpirationDate(),
                    cardData.getCardHolderName()
                );
                
                PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
                        .setData(ByteString.copyFromUtf8(message))
                        .build();
                
                publisher.publish(pubsubMessage);
                publisher.shutdown();
                log.info("Evento order-created publicado para orden: {} (Emulador: {})", order.getId(), emulatorHost != null ? "Sí" : "No");
            } else {
                log.warn("⚠️ Pub/Sub no configurado. Evento order-created SIMULADO para orden: {}. " +
                        "Para usar Pub/Sub real, configura PUBSUB_EMULATOR_HOST o GCP_PROJECT_ID", order.getId());
            }
        } catch (Exception e) {
            log.error("Error al publicar evento de pago", e);
        }
    }

    private OrderResponse mapToResponse(Order order, List<OrderItem> items) {
        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .cardToken(order.getCardToken())
                .deliveryAddress(order.getDeliveryAddress())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .transactionId(order.getTransactionId())
                .paymentAttempts(order.getPaymentAttempts())
                .items(items.stream().map(this::mapItemToResponse).collect(Collectors.toList()))
                .build();
    }

    private com.farmatodo.order.dto.OrderItemResponse mapItemToResponse(OrderItem item) {
        return com.farmatodo.order.dto.OrderItemResponse.builder()
                .id(item.getId())
                .orderId(item.getOrderId())
                .productId(item.getProductId())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }
}

