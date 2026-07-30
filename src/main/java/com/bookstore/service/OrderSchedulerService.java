package com.bookstore.service;

import com.bookstore.enums.OrderStatus;
import com.bookstore.enums.PaymentStatus;
import com.bookstore.model.Order;
import com.bookstore.model.Payment;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.OrderRepository;
import com.bookstore.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderSchedulerService {
    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;
    private final PaymentRepository paymentRepository;

    private static final int PENDING_TIMEOUT_MINUTES = 30;
    private static final int BATCH_SIZE = 50;

    @Scheduled(cron = "0 * * * * *") // Chạy mỗi 1 phút
    @Transactional
    public void cancelPendingOrders() {
        log.info("⏰ [Scheduled] Running cancelPendingOrders...");

        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(PENDING_TIMEOUT_MINUTES);

        List<Order> pendingOrders = orderRepository.findByStatusAndOrderDateBefore(OrderStatus.PENDING, timeoutThreshold);
        if (pendingOrders.isEmpty()) {
            log.info("No pending orders to cancel.");
            return;
        }
        log.info("📋 Found {} pending orders to cancel", pendingOrders.size());

        int cancelledCount = 0;
        int restockCount = 0;

        for (Order order : pendingOrders) {
            try {
                cancelOrderAndRestock(order);
                cancelledCount++;
                restockCount += order.getOrderItems().size();
            } catch (Exception e) {
                log.error("Error occurred while canceling order {}", order.getId(), e);
            }
        }
        log.info("✅ Canceled {} orders and restocked {} books", cancelledCount, restockCount);
    }

    public void cancelOrderAndRestock(Order order) {
        if (order.getStatus() != OrderStatus.PENDING || order.getIsAutoCancelled()) {
            log.warn("Order {} is not pending or already cancelled", order.getId());
            return;
        }
        // Cập nhật trạng thái đơn hàng
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelledReason("Auto-cancelled: Payment timeout after " + PENDING_TIMEOUT_MINUTES + " minutes");
        order.setIsAutoCancelled(true);
        orderRepository.save(order);
        log.info("❌ Order {} canceled", order.getId());

        // Hoàn trả số lượng sách về kho
        order.getOrderItems().forEach(item -> {
            bookRepository.findById(item.getBook().getId()).ifPresent(book -> {
                book.increaseStock(item.getQuantity());
                bookRepository.save(book);
                log.info("📦 Restocked {} units of book {}", item.getQuantity(), book.getTitle());
            });
        });

        Payment payment = order.getPayment();
        if(payment != null) {
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
            log.info("💳 Payment for order {} refunded", order.getId());
        }
    }
}
