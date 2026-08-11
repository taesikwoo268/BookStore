package com.bookstore.strategy;

import com.bookstore.enums.OrderStatus;
import com.bookstore.strategy.state.OrderStateTransition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStateTransitionFactory {

    private final List<OrderStateTransition> transitions;

    public OrderStateTransition getTransition(OrderStatus currentStatus, OrderStatus targetStatus) {
        return transitions.stream()
                .filter(t -> t.canTransition(currentStatus, targetStatus))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("❌ No transition found from {} to {}", currentStatus, targetStatus);
                    return new RuntimeException(
                            String.format("Invalid transition from %s to %s", currentStatus, targetStatus)
                    );
                });
    }

    public boolean isValidTransition(OrderStatus currentStatus, OrderStatus targetStatus) {
        return transitions.stream()
                .anyMatch(t -> t.canTransition(currentStatus, targetStatus));
    }
}