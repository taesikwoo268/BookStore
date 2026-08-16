package com.bookstore.strategy.state;

import com.bookstore.model.Order;
import com.bookstore.enums.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class ConfirmedStateTransition implements OrderStateTransition {

    @Override
    public boolean canTransition(OrderStatus currentStatus, OrderStatus targetStatus) {
        if (currentStatus != OrderStatus.CONFIRMED) {
            return false;
        }

        return targetStatus == OrderStatus.PROCESSING ||
                targetStatus == OrderStatus.SHIPPED ||
                targetStatus == OrderStatus.CANCELLED;
    }

    @Override
    public Order execute(Order order, OrderStatus targetStatus, String reason) {
        log.info("Transitioning order {} from CONFIRMED to {}", order.getId(), targetStatus);

        order.setStatus(targetStatus);

        switch (targetStatus) {
            case PROCESSING -> {
                order.setProcessingAt(LocalDateTime.now());
                log.info("⚙️ Order {} processing", order.getId());
            }
            case SHIPPED -> {
                order.setShippedAt(LocalDateTime.now());
                log.info("🚚 Order {} shipped", order.getId());
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