package com.bookstore.observer;

import com.bookstore.event.OrderPlacedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmailObserver implements OrderObserver {

    @Override
    public void onOrderPlaced(OrderPlacedEvent event) {
        log.info("📧 [EmailObserver] Processing order {} for user {}",
                event.getOrderId(), event.getUserId());

        try {
            // ===== 1. BUILD EMAIL CONTENT =====
            String emailContent = buildEmailContent(event);

            // ===== 2. SEND EMAIL =====
            sendEmail(event.getUserEmail(), "Order Confirmation #" + event.getOrderNumber(), emailContent);

            log.info("✅ [EmailObserver] Order confirmation email sent to {} for order {}",
                    event.getUserEmail(), event.getOrderId());

        } catch (Exception e) {
            log.error("❌ [EmailObserver] Failed to send email for order {}: {}",
                    event.getOrderId(), e.getMessage());
        }
    }

    @Override
    public int getPriority() {
        return 1; // Chạy sau LoyaltyObserver
    }

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
        sb.append("Total: $").append(event.getTotalAmount()).append("\n");
        sb.append("\n");
        sb.append("Items:\n");
        for (OrderPlacedEvent.OrderItemEvent item : event.getItems()) {
            sb.append("  - ").append(item.getQuantity()).append("x ")
                    .append(item.getBookTitle()).append(": $")
                    .append(item.getSubtotal()).append("\n");
        }
        sb.append("\n");
        sb.append("Shipping Address:\n");
        sb.append("  ").append(event.getShippingAddress()).append("\n");
        sb.append("\n");
        sb.append("=====================================\n");
        sb.append("  Thank you for shopping with us!\n");
        sb.append("=====================================\n");
        return sb.toString();
    }

    private void sendEmail(String to, String subject, String content) {
        // Mô phỏng gửi email
        log.info("📧 Sending email to: {}", to);
        log.info("📧 Subject: {}", subject);
        log.info("📧 Content:\n{}", content);

        try {
            Thread.sleep(100); // Giả lập thời gian gửi email
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}