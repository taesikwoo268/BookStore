package com.bookstore.event.consumer;

import com.bookstore.event.OrderPlacedEvent;
import com.bookstore.model.Book;
import com.bookstore.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryReservedConsumer {

    private final BookRepository bookRepository;

    /**
     * Consume OrderPlacedEvent từ RabbitMQ
     * Cập nhật sales_count cho từng sách
     */
    @RabbitListener(queues = "${rabbitmq.queue.inventory}")
    @Transactional
    public void consumeOrderPlacedEvent(OrderPlacedEvent event) {
        log.info("📦 [InventoryReservedConsumer] Received OrderPlacedEvent: orderId={}", event.getOrderId());

        try {
            // ===== 1. LOG THÔNG TIN ORDER =====
            log.info("📦 Order ID: {}, User: {}, Total: ${}",
                    event.getOrderId(), event.getUserId(), event.getTotalAmount());

            // ===== 2. DUYỆT TỪNG ITEM =====
            if (event.getItems() == null || event.getItems().isEmpty()) {
                log.warn("📦 No items in order: {}", event.getOrderId());
                return;
            }

            int updatedCount = 0;
            for (OrderPlacedEvent.OrderItemEvent item : event.getItems()) {
                // ===== 2.1. Tìm sách =====
                Book book = bookRepository.findById(item.getBookId())
                        .orElseThrow(() -> new RuntimeException("Book not found: " + item.getBookId()));

                // ===== 2.2. Lưu giá trị cũ =====
                int oldSalesCount = book.getSalesCount() != null ? book.getSalesCount() : 0;

                // ===== 2.3. Cập nhật sales_count =====
                int newSalesCount = oldSalesCount + item.getQuantity();
                book.setSalesCount(newSalesCount);

                // ===== 2.4. Lưu vào database =====
                bookRepository.save(book);

                // ===== 2.5. LOG =====
                log.info("📊 Updated sales count for book: '{}' (ID: {})",
                        book.getTitle(), book.getId());
                log.info("   📈 {} → {} (added {})",
                        oldSalesCount, newSalesCount, item.getQuantity());

                updatedCount++;
            }

            log.info("✅ [InventoryReservedConsumer] Updated {} books sales count for order {}",
                    updatedCount, event.getOrderId());

        } catch (Exception e) {
            log.error("❌ [InventoryReservedConsumer] Failed to update inventory for order {}: {}",
                    event.getOrderId(), e.getMessage(), e);
        }
    }
}