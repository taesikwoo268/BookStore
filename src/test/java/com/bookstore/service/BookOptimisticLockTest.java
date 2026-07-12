package com.bookstore.service;

import com.bookstore.dto.request.BookUpdateRequest;
import com.bookstore.model.Book;
import com.bookstore.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BookOptimisticLockTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    private Long bookId;
    private Book book;

    @BeforeEach
    void setUp() {
        // Tạo một book test
        Book newBook = Book.builder()
                .isbn("978-1234567890")
                .title("Test Lock Book")
                .price(new BigDecimal("19.99"))
                .stock(100)
                .salesCount(0)
                .version(0)
                .build();
        book = bookRepository.save(newBook);
        bookId = book.getId();
    }

    @Test
    void testOptimisticLockException() throws InterruptedException {
        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // Mỗi thread sẽ update cùng một book với title khác nhau
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    // Tạo request với title khác nhau
                    BookUpdateRequest request = BookUpdateRequest.builder()
                            .isbn("978-1234567890")
                            .title("Updated by Thread " + index)
                            .price(new BigDecimal("29.99"))
                            .stock(100)
                            .build();

                    // Cả 2 thread cùng update
                    bookService.updateBookWithVersion(bookId, request);
                    successCount.incrementAndGet();

                } catch (OptimisticLockingFailureException e) {
                    failureCount.incrementAndGet();
                    System.out.println("Thread " + index + " gặp OptimisticLockException: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        // Đợi tất cả thread hoàn thành (timeout 10s)
        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Kiểm tra kết quả
        System.out.println("Success: " + successCount.get() + ", Failure: " + failureCount.get());

        // Ít nhất 1 thread phải thất bại do OptimisticLockException
        assertTrue(failureCount.get() > 0, "Expected at least one optimistic lock failure");
        assertTrue(successCount.get() > 0, "Expected at least one update success");
    }
}