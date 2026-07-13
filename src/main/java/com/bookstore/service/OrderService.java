package com.bookstore.service;

import com.bookstore.exception.BookNotFoundException;
import com.bookstore.exception.InsufficientStockException;
import com.bookstore.model.Book;
import com.bookstore.model.Order;
import com.bookstore.model.OrderItem;
import com.bookstore.model.User;
import com.bookstore.model.enums.OrderStatus;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;

    /**
     * Đặt hàng với cơ chế Optimistic Lock để tránh oversell
     */
    @Retryable(
            value = {OptimisticLockingFailureException.class},
            maxAttempts = 5,
            backoff = @Backoff(delay = 50, multiplier = 2)
    )
    @Transactional
    public Order placeOrder(Long bookId, Long userId, int quantity) {
        // 1. Lấy thông tin sách (có version để optimistic lock)
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + bookId));

        // 2. Kiểm tra tồn kho
        if (book.getStock() < quantity) {
            throw new InsufficientStockException(
                    "Not enough stock. Requested: " + quantity + ", Available: " + book.getStock()
            );
        }

        // 3. Trừ tồn kho (version sẽ tự động tăng)
        book.setStock(book.getStock() - quantity);
        book.setSalesCount(book.getSalesCount() + quantity);

        // 4. Lưu sách - nếu có conflict sẽ throw OptimisticLockingFailureException
        Book savedBook = bookRepository.save(book);

        // 5. Tạo đơn hàng
        Order order = createOrder(book, userId, quantity);
        return orderRepository.save(order);
    }

    /**
     * Đặt hàng với Pessimistic Lock (SELECT ... FOR UPDATE) để tránh oversell
     */
    @Transactional
    public Order placeOrderWithPessimisticLock(Long bookId, Long userId, int quantity) {

        // 1. Lấy sách với Pessimistic Lock (SELECT ... FOR UPDATE)
        Book book = bookRepository.findByIdWithPessimisticLock(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + bookId));
        // 2. Kiểm tra tồn kho
        if (book.getStock() < quantity) {
            throw new InsufficientStockException(
                    "Not enough stock. Requested: " + quantity + ", Available: " + book.getStock()
            );
        }
        // 3. Trừ tồn kho
        book.setStock(book.getStock() - quantity);
        book.setSalesCount(book.getSalesCount() + quantity);
        Book savedBook = bookRepository.save(book);

        // 4. Tạo đơn hàng
        Order order = createOrder(book, userId, quantity);
        return orderRepository.save(order);
    }

    /**
     * Tạo đơn hàng từ thông tin sách
     */
    private Order createOrder(Book book, Long userId, int quantity) {
        Order order = Order.builder()
                .user(User.builder().id(userId).build())
                .status(OrderStatus.PENDING.name())
                .orderDate(LocalDateTime.now())
                .totalAmount(book.getPrice().multiply(BigDecimal.valueOf(quantity)))
                .build();

        OrderItem orderItem = OrderItem.builder()
                .book(book)
                .quantity(quantity)
                .price(book.getPrice())
                .build();

        order.addOrderItem(orderItem);
        order.calculateTotal();

        return order;
    }
}