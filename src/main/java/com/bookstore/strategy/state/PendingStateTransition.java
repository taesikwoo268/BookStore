package com.bookstore.strategy.state;

import com.bookstore.model.Order;
import com.bookstore.enums.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class PendingStateTransition implements OrderStateTransition {

    @Override
    public boolean canTransition(OrderStatus currentStatus, OrderStatus targetStatus) {
        if (currentStatus != OrderStatus.PENDING) {
            return false;
        }

        return targetStatus == OrderStatus.CONFIRMED ||
                targetStatus == OrderStatus.PROCESSING ||
                targetStatus == OrderStatus.CANCELLED;
    }

    @Override
    public Order execute(Order order, OrderStatus targetStatus, String reason) {
        log.info("Transitioning order {} from PENDING to {}", order.getId(), targetStatus);

        order.setStatus(targetStatus);

        switch (targetStatus) {
            case CONFIRMED -> {
                order.setConfirmedAt(LocalDateTime.now());
                log.info("✅ Order {} confirmed", order.getId());
            }
            case PROCESSING -> {
                order.setProcessingAt(LocalDateTime.now());
                log.info("⚙️ Order {} processing", order.getId());
            }
            case CANCELLED -> {
                order.setCancelledAt(LocalDateTime.now());
                order.setCancelledReason(reason);
                log.info("❌ Order {} cancelled", order.getId());
            }
            default -> throw new RuntimeException("Invalid target status: " + targetStatus);
        }

        return order;
    }
}