package com.bookstore.event.consumer;

import com.bookstore.event.OrderPlacedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationConsumer {

    private final ObjectMapper objectMapper;

    /**
     * Consume OrderPlacedEvent từ RabbitMQ queue
     * Gửi email xác nhận đơn hàng cho user
     */
    @RabbitListener(queues = "${rabbitmq.queue.email}")
    public void consumeOrderPlacedEvent(OrderPlacedEvent event) {
        log.info("📧 [EmailNotificationConsumer] Received OrderPlacedEvent: orderId={}", event.getOrderId());

        try {
            // ===== 1. LOG THÔNG TIN EMAIL =====
            String email = event.getUserEmail();
            Long orderId = event.getOrderId();
            String orderNumber = event.getOrderNumber();

            log.info("📧 Sending email to {} for order {}", email, orderId);
            log.info("📧 Order details: orderNumber={}, totalAmount={}", orderNumber, event.getTotalAmount());

            // ===== 2. LOG CHI TIẾT ITEMS =====
            if (event.getItems() != null && !event.getItems().isEmpty()) {
                log.info("📧 Order items:");
                for (OrderPlacedEvent.OrderItemEvent item : event.getItems()) {
                    log.info("   - {} x {}: ${}",
                            item.getQuantity(),
                            item.getBookTitle(),
                            item.getSubtotal()
                    );
                }
            }

            // ===== 3. LOG SHIPPING ADDRESS =====
            log.info("📧 Shipping address: {}", event.getShippingAddress());

            // ===== 4. GỬI EMAIL (MÔ PHỎNG) =====
            // Trong thực tế, gọi service gửi email ở đây
            // emailService.sendOrderConfirmation(email, event);
            sendEmail(event);

            log.info("✅ [EmailNotificationConsumer] Email sent successfully for order {}", orderId);

        } catch (Exception e) {
            log.error("❌ [EmailNotificationConsumer] Failed to send email for order {}: {}",
                    event.getOrderId(), e.getMessage(), e);
        }
    }

    /**
     * Mô phỏng gửi email
     */
    private void sendEmail(OrderPlacedEvent event) {
        // Mô phỏng thời gian gửi email
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Log nội dung email
        String emailContent = buildEmailContent(event);
        log.info("📧 Email content:\n{}", emailContent);
    }

    /**
     * Xây dựng nội dung email
     */
    private String buildEmailContent(OrderPlacedEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("=====================================\n");
        sb.append("     📧 ORDER CONFIRMATION\n");
        sb.append("=====================================\n");
        sb.append("\n");
        sb.append("Dear Customer,\n");
        sb.append("\n");
        sb.append("Thank you for your order!\n");
        sb.append("\n");
        sb.append("Order ID: ").append(event.getOrderNumber()).append("\n");
        sb.append("Order Date: ").append(event.getEventTime()).append("\n");
        sb.append("\n");
        sb.append("-------------------------------------\n");
        sb.append("  ORDER ITEMS\n");
        sb.append("-------------------------------------\n");

        for (OrderPlacedEvent.OrderItemEvent item : event.getItems()) {
            sb.append(String.format("  %d x %s\n", item.getQuantity(), item.getBookTitle()));
            sb.append(String.format("    Price: $%.2f\n", item.getPrice()));
            sb.append(String.format("    Subtotal: $%.2f\n", item.getSubtotal()));
            sb.append("\n");
        }

        sb.append("-------------------------------------\n");
        sb.append(String.format("  Total: $%.2f\n", event.getTotalAmount()));
        sb.append("-------------------------------------\n");
        sb.append("\n");
        sb.append("Shipping Address:\n");
        sb.append("  ").append(event.getShippingAddress()).append("\n");
        sb.append("\n");
        sb.append("=====================================\n");
        sb.append("  Thank you for shopping with us!\n");
        sb.append("=====================================\n");

        return sb.toString();
    }
}