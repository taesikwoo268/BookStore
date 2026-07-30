package com.bookstore.service;

import com.bookstore.enums.OrderStatus;
import com.bookstore.exception.InvalidStateTransitionException;
import com.bookstore.model.Order;
import com.bookstore.repository.OrderRepository;
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

    @Transactional
    public Order transitionTo(Long orderId, OrderStatus targetStatus,String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        return transitionTo(order,targetStatus,reason);
    }

    @Transactional
    public Order transitionTo (Order order, OrderStatus targetStatus,String reason) {
        OrderStatus currentStatus = order.getStatus();

        if (!currentStatus.canTransitionTo(targetStatus)) {
            log.warn("❌ Invalid transition: {} → {}", currentStatus, targetStatus);
            throw new InvalidStateTransitionException(
                    currentStatus.name(),
                    targetStatus.name(),
                    String.format("Order %d cannot transition from %s to %s",
                            order.getId(), currentStatus, targetStatus)
            );
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(targetStatus);

        switch (targetStatus) {
            case CONFIRMED:
                order.setConfirmedAt(LocalDateTime.now());
                break;
            case PROCESSING:
                order.setProcessingAt(LocalDateTime.now());
                break;
            case SHIPPED:
                order.setShippedAt(LocalDateTime.now());
                break;
            case DELIVERED:
                order.setDeliveredAt(LocalDateTime.now());
                break;
            case CANCELLED:
                order.setCancelledAt(LocalDateTime.now());
                break;
            case REFUNDED:
                order.setRefundedAt(LocalDateTime.now());
                break;
            default:
                break;
        }
        Order savedOrder = orderRepository.save(order);
        log.info("✅ Order {} transitioned: {} → {} (reason: {})",
                order.getId(), oldStatus, targetStatus, reason != null ? reason : "N/A");

        return savedOrder;
    }

    /**
     * Xác nhận đơn hàng (PENDING → CONFIRMED)
     */
    public Order confirmOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidStateTransitionException(
                    order.getStatus().name(),
                    OrderStatus.CONFIRMED.name(),
                    "Only PENDING orders can be confirmed"
            );
        }

        return transitionTo(order, OrderStatus.CONFIRMED, "Admin confirmed");
    }

    /**
     * Hủy đơn hàng (PENDING/CONFIRMED/PROCESSING → CANCELLED)
     */
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

        return transitionTo(order, OrderStatus.CANCELLED, reason);
    }

    /**
     * Giao hàng (SHIPPED → DELIVERED)
     */
    public Order deliverOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new InvalidStateTransitionException(
                    order.getStatus().name(),
                    OrderStatus.DELIVERED.name(),
                    "Only SHIPPED orders can be delivered"
            );
        }

        return transitionTo(order, OrderStatus.DELIVERED, "Customer received");
    }
}
