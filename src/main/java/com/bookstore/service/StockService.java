package com.bookstore.service;

import com.bookstore.exception.BookNotFoundException;
import com.bookstore.exception.InsufficientStockException;
import com.bookstore.model.Book;
import com.bookstore.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.retry.annotation.Backoff;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {
    private final BookRepository bookRepository;

    private static final int  MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY = 100;

    @Retryable(
            value = {OptimisticLockingFailureException.class},
            maxAttempts = MAX_RETRY_ATTEMPTS,
            backoff = @Backoff(delay = RETRY_DELAY, multiplier = 2)
    )
    @Transactional
    public void deductStockWithRetry(Long bookId, int quantity) {
        log.info("📦 Deducting stock for bookId: {}, quantity: {}", bookId, quantity);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + bookId));

        if(book.getStock() < quantity) {
            throw new InsufficientStockException("Not enough stock. Available: " + book.getStock());
        }
        book.reduceStock(quantity);
        bookRepository.save(book);
        log.info("✅ Stock deducted successfully. Book: {}, New stock: {}, Version: {}",
                book.getTitle(), book.getStock(), book.getVersion());
    }

    @Retryable(
            value = {OptimisticLockingFailureException.class},
            maxAttempts = MAX_RETRY_ATTEMPTS,
            backoff = @Backoff(delay = RETRY_DELAY, multiplier = 2)
    )
    @Transactional
    public Book increaseStock(Long bookId, int quantity) {
        log.info("📦 Increasing stock for bookId: {}, quantity: {}", bookId, quantity);

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found: " + bookId));

        book.increaseStock(quantity);
        Book savedBook = bookRepository.save(book);

        log.info("✅ Stock increased successfully. Book: {}, New stock: {}, Version: {}",
                savedBook.getTitle(), savedBook.getStock(), savedBook.getVersion());

        return savedBook;
    }
}
