package com.bookstore.event.publisher;

import com.bookstore.event.OrderPlacedEvent;
import com.bookstore.event.OrderSubject;
import com.bookstore.model.Order;
import com.bookstore.model.OrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final OrderSubject orderSubject;

    @Value("${rabbitmq.exchange.order}")
    private String orderExchange;


    /**
     * Publish OrderPlacedEvent sau khi checkout thành công
     */
    public void publishOrderPlacedEvent(Order order) {
        log.info("📤 Publishing OrderPlacedEvent for order: {}", order.getId());

        OrderPlacedEvent event = buildOrderPlacedEvent(order);

        try {
            // Publish message
            rabbitTemplate.convertAndSend(
                    orderExchange,
                    "order.placed.email",   // ✅ Routing key cho email
                    event
            );

            rabbitTemplate.convertAndSend(
                    orderExchange,
                    "order.placed.loyalty",  // ✅ Routing key cho loyalty
                    event
            );

            rabbitTemplate.convertAndSend(
                    orderExchange,
                    "order.placed.inventory",  // ✅ Routing key cho inventory
                    event
            );

            log.info("✅ OrderPlacedEvent published successfully: orderId={}, eventId={}",
                    order.getId(), event.getEventId());

        } catch (Exception e) {
            log.error("❌ Failed to publish OrderPlacedEvent: {}", e.getMessage(), e);
            // Không throw exception để không ảnh hưởng đến flow checkout
        }

        orderSubject.notifyObservers(event);
    }

    /**
     * Build OrderPlacedEvent từ Order entity
     */
    private OrderPlacedEvent buildOrderPlacedEvent(Order order) {
        return OrderPlacedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("ORDER_PLACED")
                .eventTime(LocalDateTime.now())
                .orderId(order.getId())
                .orderNumber("ORD-" + String.format("%08d", order.getId()))
                .userId(order.getUser().getId())
                .userEmail(order.getUser().getEmail())
                .items(order.getOrderItems().stream()
                        .map(this::convertToItemEvent)
                        .collect(Collectors.toList()))
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .build();
    }

    private OrderPlacedEvent.OrderItemEvent convertToItemEvent(OrderItem item) {
        return OrderPlacedEvent.OrderItemEvent.builder()
                .bookId(item.getBook().getId())
                .bookTitle(item.getBook().getTitle())
                .bookIsbn(item.getBook().getIsbn())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .subtotal(item.getSubtotal())
                .build();
    }
}