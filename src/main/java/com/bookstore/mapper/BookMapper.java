package com.bookstore.mapper;

import com.bookstore.model.Book;
import com.bookstore.model.Author;
import com.bookstore.model.Category;

import java.math.BigDecimal;
import java.util.List;

// DTO nếu cần, ở đây tôi chỉ tạo phương thức tiện ích
public class BookMapper {
    public static Book toBook(String isbn, String title, BigDecimal price, Integer stock,
                              List<Author> authors, Category category) {
        return Book.builder()
                .isbn(isbn)
                .title(title)
                .price(price)
                .stock(stock)
                .salesCount(0)
                .authors(authors)
                .category(category)
                .build();
    }
}