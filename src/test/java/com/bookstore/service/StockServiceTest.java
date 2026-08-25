//package com.bookstore.service;
//
//import com.bookstore.model.Book;
//import com.bookstore.repository.BookRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@ActiveProfiles("test")  // ✅ Dùng H2 database
//@Transactional           // ✅ Rollback sau mỗi test
//@DisplayName("StockService - Concurrent Tests")
//class StockServiceTest {
//
//    @Autowired
//    private StockService stockService;
//
//    @Autowired
//    private BookRepository bookRepository;
//
//    // ✅ ISBN cố định (13 ký tự)
//    private static final String TEST_ISBN = "978-9999999989";
//    private static final String TEST_ISBN_2 = "978-8888888888";
//    private static final String TEST_ISBN_3 = "978-7777777777";
//
//    private Long bookId;
//
//    @BeforeEach
//    void setUp() {
//        // ✅ Dùng ISBN cố định cho mỗi test
//        Book book = Book.builder()
//                .isbn(TEST_ISBN)
//                .title("Test Lock Book")
//                .price(new BigDecimal("19.99"))
//                .stock(1)
//                .salesCount(0)
//                .version(0)
//                .build();
//
//        book = bookRepository.save(book);
//        bookId = book.getId();
//
//        System.out.println("✅ Created book with ISBN: " + TEST_ISBN + ", stock: 1");
//    }
//
//    // ============================================================
//    // 1. CONCURRENT DEDUCT STOCK
//    // ============================================================
//
//    @Test
//    @DisplayName("5 concurrent threads deduct stock - only 1 succeeds")
//    void testConcurrentDeductStock_ShouldNotOversell() throws InterruptedException {
//        int threadCount = 5;
//        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
//        CountDownLatch latch = new CountDownLatch(threadCount);
//
//        AtomicInteger successCount = new AtomicInteger(0);
//        AtomicInteger failureCount = new AtomicInteger(0);
//
//        for (int i = 0; i < threadCount; i++) {
//            final int threadId = i;
//            executor.submit(() -> {
//                try {
//                    stockService.deductStockWithRetry(bookId, 1);
//                    successCount.incrementAndGet();
//                    System.out.println("✅ Thread " + threadId + " SUCCESS!");
//                } catch (Exception e) {
//                    failureCount.incrementAndGet();
//                    System.out.println("❌ Thread " + threadId + " FAILED: " + e.getMessage());
//                } finally {
//                    latch.countDown();
//                }
//            });
//        }
//
//        latch.await(10, TimeUnit.SECONDS);
//        executor.shutdown();
//
//        System.out.println("\n📊 RESULTS:");
//        System.out.println("✅ Success: " + successCount.get());
//        System.out.println("❌ Failure: " + failureCount.get());
//
//        assertEquals(1, successCount.get());
//        assertEquals(threadCount - 1, failureCount.get());
//
//        Book finalBook = bookRepository.findById(bookId).orElseThrow();
//        assertEquals(0, finalBook.getStock());
//        assertEquals(1, finalBook.getSalesCount());
//        assertTrue(finalBook.getVersion() > 0);
//    }
//
//    // ============================================================
//    // 2. NORMAL FLOW
//    // ============================================================
//
//    @Test
//    @DisplayName("Deduct stock - normal flow with sufficient stock")
//    void testDeductStock_NormalFlow_Success() {
//        // ✅ Dùng ISBN cố định khác
//        Book book = Book.builder()
//                .isbn(TEST_ISBN_2)
//                .title("Normal Test Book")
//                .price(new BigDecimal("19.99"))
//                .stock(10)
//                .salesCount(0)
//                .version(0)
//                .build();
//        book = bookRepository.save(book);
//
//        Book updated = stockService.deductStockWithRetry(book.getId(), 3);
//
//        assertEquals(7, updated.getStock());
//        assertEquals(3, updated.getSalesCount());
//    }
//
//    // ============================================================
//    // 3. INSUFFICIENT STOCK
//    // ============================================================
//
//    @Test
//    @DisplayName("Deduct stock - insufficient stock throws exception")
//    void testDeductStock_InsufficientStock_ThrowsException() {
//        // ✅ Dùng ISBN cố định khác
//        Book book = Book.builder()
//                .isbn(TEST_ISBN_3)
//                .title("Insufficient Stock Book")
//                .price(new BigDecimal("19.99"))
//                .stock(2)
//                .salesCount(0)
//                .version(0)
//                .build();
//        book = bookRepository.save(book);
//        Long bookId = book.getId();
//
//        assertThatThrownBy(() -> stockService.deductStockWithRetry(bookId, 5))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessageContaining("Not enough stock");
//
//        Book finalBook = bookRepository.findById(book.getId()).orElseThrow();
//        assertEquals(2, finalBook.getStock());
//    }
//
//    // ============================================================
//    // 4. 10 THREADS
//    // ============================================================
//
//    @Test
//    @DisplayName("10 concurrent threads deduct stock - only 1 succeeds")
//    void testConcurrentDeductStock_10Threads_ShouldNotOversell() throws InterruptedException {
//        int threadCount = 10;
//        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
//        CountDownLatch latch = new CountDownLatch(threadCount);
//
//        AtomicInteger successCount = new AtomicInteger(0);
//        AtomicInteger failureCount = new AtomicInteger(0);
//
//        for (int i = 0; i < threadCount; i++) {
//            executor.submit(() -> {
//                try {
//                    stockService.deductStockWithRetry(bookId, 1);
//                    successCount.incrementAndGet();
//                } catch (Exception e) {
//                    failureCount.incrementAndGet();
//                } finally {
//                    latch.countDown();
//                }
//            });
//        }
//
//        latch.await(15, TimeUnit.SECONDS);
//        executor.shutdown();
//
//        assertEquals(1, successCount.get());
//        assertEquals(threadCount - 1, failureCount.get());
//
//        Book finalBook = bookRepository.findById(bookId).orElseThrow();
//        assertEquals(0, finalBook.getStock());
//    }
//}