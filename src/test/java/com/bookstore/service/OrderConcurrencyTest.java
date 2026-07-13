package com.bookstore.service;

import com.bookstore.exception.InsufficientStockException;
import com.bookstore.model.Book;
import com.bookstore.model.Order;
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
class OrderConcurrencyTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private BookRepository bookRepository;

    private Long bookId;
    private final int INITIAL_STOCK = 1;
    private final int USER_COUNT = 100;
    private final int QUANTITY_PER_USER = 1;

    @BeforeEach
    void setUp() {
        bookRepository.findByIsbn("978-9999999999")
                .ifPresent(b -> bookRepository.delete(b));

        Book book = Book.builder()
                .isbn("978-9999999999")
                .title("Concurrency Test Book")
                .price(new BigDecimal("19.99"))
                .stock(INITIAL_STOCK)
                .salesCount(0)
                .version(0)
                .build();

        Book saved = bookRepository.save(book);
        bookId = saved.getId();

        System.out.println("✅ Created book with ID: " + bookId + ", stock: " + INITIAL_STOCK);
    }

    @Test
    void test100UsersConcurrentOrder_WithOptimisticLock() throws InterruptedException {
        System.out.println("\n🚀 STARTING CONCURRENT ORDER TEST");
        System.out.println("📊 " + USER_COUNT + " users, " + INITIAL_STOCK + " book(s) available");

        ExecutorService executor = Executors.newFixedThreadPool(USER_COUNT);
        CountDownLatch latch = new CountDownLatch(USER_COUNT);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger insufficientStockCount = new AtomicInteger(0);
        AtomicInteger optimisticLockCount = new AtomicInteger(0);
        AtomicInteger otherErrorCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < USER_COUNT; i++) {
            final int userId = i + 1;
            executor.submit(() -> {
                try {
                    System.out.println("👤 User " + userId + " is placing order...");

                    Order order = orderService.placeOrder(
                            bookId,
                            (long) userId,
                            QUANTITY_PER_USER
                    );

                    successCount.incrementAndGet();
                    System.out.println("✅ User " + userId + " placed order successfully. Order ID: " + order.getId());

                } catch (InsufficientStockException e) {
                    insufficientStockCount.incrementAndGet();
                    System.out.println("❌ User " + userId + " failed: Insufficient stock");

                } catch (OptimisticLockingFailureException e) {
                    optimisticLockCount.incrementAndGet();
                    System.out.println("❌ User " + userId + " failed: Optimistic lock conflict");

                } catch (Exception e) {
                    otherErrorCount.incrementAndGet();
                    System.out.println("❌ User " + userId + " failed: " + e.getClass().getSimpleName());
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        long endTime = System.currentTimeMillis();

        System.out.println("\n📊 ====== TEST RESULTS ======");
        System.out.println("⏱️ Total time: " + (endTime - startTime) + "ms");
        System.out.println("✅ Successful orders: " + successCount.get());
        System.out.println("❌ Insufficient stock: " + insufficientStockCount.get());
        System.out.println("❌ Optimistic lock conflicts: " + optimisticLockCount.get());
        System.out.println("❌ Other errors: " + otherErrorCount.get());
        System.out.println("📚 Total attempts: " + USER_COUNT);

        Book finalBook = bookRepository.findById(bookId).orElseThrow();
        System.out.println("\n📖 Final book state:");
        System.out.println("   - Stock: " + finalBook.getStock());
        System.out.println("   - Sales count: " + finalBook.getSalesCount());
        System.out.println("   - Version: " + finalBook.getVersion());

        // Assertions
        assertEquals(1, successCount.get(), "Only 1 order should succeed");
        assertEquals(0, finalBook.getStock(), "Stock should be 0");
        assertEquals(1, finalBook.getSalesCount(), "Sales count should be 1");

        int totalFailures = insufficientStockCount.get() + optimisticLockCount.get() + otherErrorCount.get();
        assertEquals(99, totalFailures, "99 users should fail");

        assertTrue(completed, "All threads should complete");

        System.out.println("\n✅ TEST PASSED: NO OVERSELL!");
    }

    @Test
    void test100UsersConcurrentOrder_WithPessimisticLock() throws InterruptedException {
        System.out.println("\n🚀 STARTING CONCURRENT ORDER TEST (PESSIMISTIC LOCK)");
        System.out.println("📊 " + USER_COUNT + " users, " + INITIAL_STOCK + " book(s) available");

        ExecutorService executor = Executors.newFixedThreadPool(USER_COUNT);
        CountDownLatch latch = new CountDownLatch(USER_COUNT);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger insufficientStockCount = new AtomicInteger(0);
        AtomicInteger otherErrorCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < USER_COUNT; i++) {
            final int userId = i + 1;
            executor.submit(() -> {
                try {
                    System.out.println("👤 User " + userId + " is placing order...");

                    Order order = orderService.placeOrderWithPessimisticLock(
                            bookId,
                            (long) userId,
                            QUANTITY_PER_USER
                    );

                    successCount.incrementAndGet();
                    System.out.println("✅ User " + userId + " placed order successfully. Order ID: " + order.getId());

                } catch (InsufficientStockException e) {
                    insufficientStockCount.incrementAndGet();
                    System.out.println("❌ User " + userId + " failed: Insufficient stock");
                } catch (Exception e) {
                    otherErrorCount.incrementAndGet();
                    System.out.println("❌ User " + userId + " failed: " + e.getClass().getSimpleName());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        long endTime = System.currentTimeMillis();

        System.out.println("\n📊 ====== TEST RESULTS (PESSIMISTIC) ======");
        System.out.println("⏱️ Total time: " + (endTime - startTime) + "ms");
        System.out.println("✅ Successful orders: " + successCount.get());
        System.out.println("❌ Insufficient stock: " + insufficientStockCount.get());
        System.out.println("❌ Other errors: " + otherErrorCount.get());

        Book finalBook = bookRepository.findById(bookId).orElseThrow();
        System.out.println("\n📖 Final book state:");
        System.out.println("   - Stock: " + finalBook.getStock());
        System.out.println("   - Sales count: " + finalBook.getSalesCount());

        assertEquals(1, successCount.get());
        assertEquals(0, finalBook.getStock());
        assertEquals(1, finalBook.getSalesCount());

        System.out.println("\n✅ TEST PASSED: NO OVERSELL!");
    }
}