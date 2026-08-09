package com.bookstore.service;

import com.bookstore.model.Book;
import com.bookstore.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StockServiceTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private BookRepository bookRepository;

    private Long bookId;

    @BeforeEach
    void setUp() {
        // Tạo book test với stock = 1
        Book book = Book.builder()
                .isbn("978-9999999999")
                .title("Test Lock Book")
                .price(new BigDecimal("19.99"))
                .stock(1)      // Chỉ có 1 quyển
                .salesCount(0)
                .version(0)
                .build();
        book = bookRepository.save(book);
        bookId = book.getId();
    }

    @Test
    void testConcurrentDeductStock_ShouldNotOversell() throws InterruptedException {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    stockService.deductStockWithRetry(bookId, 1);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Kiểm tra kết quả
        System.out.println("Success: " + successCount.get() + ", Failure: " + failureCount.get());

        // ✅ Chỉ có 1 thread thành công
        assertEquals(1, successCount.get());
        assertEquals(threadCount - 1, failureCount.get());

        // ✅ Stock về 0
        Book finalBook = bookRepository.findById(bookId).orElseThrow();
        assertEquals(0, finalBook.getStock());
    }
}