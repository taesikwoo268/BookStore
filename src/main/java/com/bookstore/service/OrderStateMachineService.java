package com.bookstore.service;

import com.bookstore.enums.OrderStatus;
import com.bookstore.exception.InvalidStateTransitionException;
import com.bookstore.model.Order;
import com.bookstore.repository.OrderRepository;
import com.bookstore.strategy.OrderStateTransitionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderStateMachineService {
    private OrderRepository orderRepository;
    private final OrderStateTransitionFactory transitionFactory;

    @Transactional
    public Order transitionTo(Long orderId, OrderStatus targetStatus,String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        return transitionTo(order,targetStatus,reason);
    }

    @Transactional
    public Order transitionTo (Order order, OrderStatus targetStatus,String reason) {
        OrderStatus currentStatus = order.getStatus();

        if (!transitionFactory.isValidTransition(currentStatus,targetStatus)) {
            log.warn("❌ Invalid transition: {} → {}", currentStatus, targetStatus);
            throw new InvalidStateTransitionException(
                    currentStatus.name(),
                    targetStatus.name(),
                    String.format("Order %d cannot transition from %s to %s",
                            order.getId(), currentStatus, targetStatus)
            );
        }

        var strategy = transitionFactory.getTransition(currentStatus,targetStatus);
        log.info("🔄 Using strategy: {} for order {}",
                strategy.getClass().getSimpleName(), order.getId());

        return strategy.execute(order, targetStatus, reason);
    }

    @Transactional
    public Order confirmOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        return transitionTo(order, OrderStatus.CONFIRMED, "Admin confirmed");
    }

    @Transactional
    public Order processOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        return transitionTo(order, OrderStatus.PROCESSING, "Start processing");
    }

    @Transactional
    public Order shipOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        return transitionTo(order, OrderStatus.SHIPPED, "Order shipped");
    }

    @Transactional
    public Order deliverOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        return transitionTo(order, OrderStatus.DELIVERED, "Customer received");
    }

    @Transactional
    public Order cancelOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (!order.getStatus().isCancellable()) {
            throw new InvalidStateTransitionException(
                    order.getStatus().name(),
                    OrderStatus.CANCELLED.name(),
                    "Order cannot be cancelled in current state"
            );
        }

        String cancelReason = reason != null ? reason : "Cancelled by user";
        return transitionTo(order, OrderStatus.CANCELLED, cancelReason);
    }

    @Transactional
    public Order refundOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        return transitionTo(order, OrderStatus.REFUNDED, "Refund processed");
    }

}
