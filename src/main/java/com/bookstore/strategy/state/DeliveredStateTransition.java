package com.bookstore.strategy.state;

import com.bookstore.model.Order;
import com.bookstore.enums.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DeliveredStateTransition implements OrderStateTransition {

    @Override
    public boolean canTransition(OrderStatus currentStatus, OrderStatus targetStatus) {
        return false; // Không thể transition từ DELIVERED
    }

    @Override
    public Order execute(Order order, OrderStatus targetStatus, String reason) {
        throw new RuntimeException("Cannot transition from DELIVERED status");
    }
}