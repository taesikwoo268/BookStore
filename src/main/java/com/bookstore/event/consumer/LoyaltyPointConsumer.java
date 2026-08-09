package com.bookstore.event.consumer;

import com.bookstore.event.OrderPlacedEvent;
import com.bookstore.service.LoyaltyPointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoyaltyPointConsumer {

    private final LoyaltyPointService loyaltyPointService;

    /**
     * Consume OrderPlacedEvent từ RabbitMQ
     * Cộng điểm tích lũy cho user
     */
    @RabbitListener(queues = "${rabbitmq.queue.loyalty}")
    public void consumeOrderPlacedEvent(OrderPlacedEvent event) {
        log.info("⭐ [LoyaltyPointConsumer] Received OrderPlacedEvent: orderId={}", event.getOrderId());

        try {
            Long userId = event.getUserId();
            Long orderId = event.getOrderId();

            // ===== 1. LOG NHẬN EVENT =====
            log.info("⭐ [LoyaltyPointConsumer] Processing loyalty points for user: {}", userId);

            // ===== 2. TÍNH ĐIỂM =====
            int points = loyaltyPointService.calculatePoints(event.getTotalAmount());
            log.info("⭐ [LoyaltyPointConsumer] Calculated {} points for order: {}", points, orderId);

            if (points > 0) {
                // ===== 3. CỘNG ĐIỂM =====
                loyaltyPointService.addLoyaltyPoints(
                        userId,
                        orderId,
                        event.getTotalAmount()
                );

                // ===== 4. LOG KẾT QUẢ =====
                String tier = loyaltyPointService.getUserTier(userId);
                int totalPoints = loyaltyPointService.getTotalPoints(userId);
                log.info("⭐ User {} earned {} points for order {}", userId, points, orderId);
                log.info("⭐ User {} total points: {}, tier: {}", userId, totalPoints, tier);

            } else {
                log.info("⭐ No points earned for order {} (totalAmount={})",
                        orderId, event.getTotalAmount());
            }

            log.info("✅ [LoyaltyPointConsumer] Processed successfully for order {}", orderId);

        } catch (Exception e) {
            log.error("❌ [LoyaltyPointConsumer] Failed to process loyalty points for order {}: {}",
                    event.getOrderId(), e.getMessage(), e);
        }
    }
}