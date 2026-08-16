package com.bookstore.strategy.state;

import com.bookstore.model.Order;
import com.bookstore.enums.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class ShippedStateTransition implements OrderStateTransition {

    @Override
    public boolean canTransition(OrderStatus currentStatus, OrderStatus targetStatus) {
        if (currentStatus != OrderStatus.SHIPPED) {
            return false;
        }

        return targetStatus == OrderStatus.DELIVERED;
    }

    @Override
    public Order execute(Order order, OrderStatus targetStatus, String reason) {
        log.info("Transitioning order {} from SHIPPED to {}", order.getId(), targetStatus);

        order.setStatus(targetStatus);
        order.setDeliveredAt(LocalDateTime.now());

        log.info("📦 Order {} delivered", order.getId());
        return order;
    }
}