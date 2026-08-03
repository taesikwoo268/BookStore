package com.bookstore.event.consumer;

import com.bookstore.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    @RabbitListener(queues = "${rabbitmq.queue.notification}")
    public void sendPushNotification(OrderPlacedEvent event) {
        log.info("🔔 [NotificationConsumer] Sending push notification for order: {}", event.getOrderId());

        try {
            String message = String.format(
                    "🎉 Order #%s placed successfully! Total: $%.2f",
                    event.getOrderNumber(),
                    event.getTotalAmount()
            );

            // Giả lập gửi push notification
            log.info("🔔 Push notification sent to user {}: {}", event.getUserId(), message);
            log.info("✅ [NotificationConsumer] Push notification sent for order: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("❌ [NotificationConsumer] Failed to send notification for order {}: {}", event.getOrderId(), e.getMessage());
        }
    }
}