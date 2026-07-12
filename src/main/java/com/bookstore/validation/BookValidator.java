package com.bookstore.validation;

import com.bookstore.model.Book;

import java.math.BigDecimal;

public class BookValidator {

    public static void validate(Book book) {
        if (book.getIsbn() == null || book.getIsbn().isBlank()) {
            throw new IllegalArgumentException("ISBN cannot be blank");
        }

        if (book.getTitle() == null || book.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title cannot be blank");
        }

        if (book.getPrice() == null || book.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be non-negative");
        }

        if (book.getStock() == null || book.getStock() < 0) {
            throw new IllegalArgumentException("Stock must be non-negative");
        }

        // Validate categories (at least one category)
        if (book.getCategories() == null || book.getCategories().isEmpty()) {
            throw new IllegalArgumentException("Book must have at least one category");
        }
    }
}