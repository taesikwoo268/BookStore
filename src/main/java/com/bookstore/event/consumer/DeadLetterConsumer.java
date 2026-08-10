package com.bookstore.event.consumer;

import com.bookstore.event.OrderPlacedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeadLetterConsumer {

    private final ObjectMapper objectMapper;

    /**
     * Consume message từ Dead Letter Queue
     * Log WARNING với orderId
     */
    @RabbitListener(queues = {
            "${rabbitmq.queue.inventory-dlq}",
            "${rabbitmq.queue.email-dlq}",
            "${rabbitmq.queue.loyalty-dlq}"
    })
    public void handleDeadLetter(Message message) {
        try {
            // ===== 1. Parse message =====
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            OrderPlacedEvent event = objectMapper.readValue(body, OrderPlacedEvent.class);

            // ===== 2. Log WARNING =====
            log.warn("💀 [DLQ] Order {} failed after 3 retries", event.getOrderId());
            log.warn("💀 [DLQ] Details: userId={}, totalAmount={}, items={}",
                    event.getUserId(),
                    event.getTotalAmount(),
                    event.getItems().size());

            // ===== 3. Log headers (retry count, reason) =====
            log.warn("💀 [DLQ] Headers: {}", message.getMessageProperties().getHeaders());

            // ===== 4. Lưu vào database để xử lý sau =====
            // saveToFailedOrdersTable(event);

        } catch (Exception e) {
            log.error("❌ [DLQ] Failed to process dead letter: {}", e.getMessage());
        }
    }
}