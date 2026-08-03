package com.bookstore.event.consumer;

import com.bookstore.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailConsumer {

    @RabbitListener(queues = "${rabbitmq.queue.email}")
    public void sendOrderConfirmationEmail(OrderPlacedEvent event) {
        log.info("📧 [EmailConsumer] Sending order confirmation email for order: {}", event.getOrderId());

        try {
            // Giả lập gửi email
            String emailContent = buildEmailContent(event);
            log.info("📧 Email sent to: {}", event.getUserEmail());
            log.info("📧 Email content:\n{}", emailContent);

            log.info("✅ [EmailConsumer] Order confirmation email sent for order: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("❌ [EmailConsumer] Failed to send email for order {}: {}", event.getOrderId(), e.getMessage());
        }
    }

    private String buildEmailContent(OrderPlacedEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("=====================================\n");
        sb.append("     ORDER CONFIRMATION\n");
        sb.append("=====================================\n");
        sb.append("Order ID: ").append(event.getOrderNumber()).append("\n");
        sb.append("Order Date: ").append(event.getEventTime()).append("\n");
        sb.append("-------------------------------------\n");

        for (OrderPlacedEvent.OrderItemEvent item : event.getItems()) {
            sb.append(item.getQuantity()).append("x ")
                    .append(item.getBookTitle()).append(" - $")
                    .append(item.getPrice()).append("\n");
        }

        sb.append("-------------------------------------\n");
        sb.append("Total Amount: $").append(event.getTotalAmount()).append("\n");
        sb.append("Shipping Address: ").append(event.getShippingAddress()).append("\n");
        sb.append("=====================================\n");
        sb.append("Thank you for your order!\n");

        return sb.toString();
    }
}