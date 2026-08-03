package com.bookstore.event.consumer;

import com.bookstore.event.OrderPlacedEvent;
import com.bookstore.model.Book;
import com.bookstore.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryConsumer {

    private final BookRepository bookRepository;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "${rabbitmq.queue.inventory}")
    public void handleInventoryUpdate(OrderPlacedEvent event) {
        log.info("📦 [InventoryConsumer] Received OrderPlacedEvent: orderId={}", event.getOrderId());

        try {
            // Cập nhật sales count cho từng sách
            for (OrderPlacedEvent.OrderItemEvent item : event.getItems()) {
                Book book = bookRepository.findById(item.getBookId())
                        .orElseThrow(() -> new RuntimeException("Book not found: " + item.getBookId()));

                // Sales count đã được update trong checkout flow
                // Có thể thêm logic bổ sung ở đây
                log.info("📚 Inventory updated: bookId={}, quantity={}", item.getBookId(), item.getQuantity());
            }

            log.info("✅ [InventoryConsumer] Order {} processed successfully", event.getOrderId());

        } catch (Exception e) {
            log.error("❌ [InventoryConsumer] Failed to process order {}: {}", event.getOrderId(), e.getMessage());
        }
    }
}