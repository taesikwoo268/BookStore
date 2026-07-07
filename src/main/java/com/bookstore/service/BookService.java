package com.bookstore.service;

import com.bookstore.dto.request.BookCreateRequest;
import com.bookstore.dto.request.BookUpdateRequest;
import com.bookstore.exception.BookNotFoundException;
import com.bookstore.exception.DuplicateISBNException;
import com.bookstore.mapper.BookMapper;
import com.bookstore.model.Author;
import com.bookstore.model.Book;
import com.bookstore.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getBookById(Long id) {
        return bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));
    }

    public Book createBook(BookCreateRequest request) {
        Book book = bookMapper.toEntity(request);
        BookValidator.validate(book);
        if (bookRepository.existsByIsbn(book.getIsbn())) {
            throw new DuplicateISBNException("Book with ISBN " + book.getIsbn() + " already exists");
        }
        return bookRepository.save(book);
    }

    public Book updateBook(Long id, BookUpdateRequest request) {
        Book existing = getBookById(id);

        // Check duplicate ISBN (excluding itself)
        if (!existing.getIsbn().equals(request.getIsbn()) &&
                bookRepository.existsByIsbn(request.getIsbn())) {
            throw new DuplicateISBNException("ISBN " + request.getIsbn() + " already used by another book");
        }

        // Map update request to existing entity
        bookMapper.updateEntity(existing, request);
        BookValidator.validate(existing);

        return bookRepository.save(existing);
    }

    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException("Book not found with id: " + id);
        }
        bookRepository.deleteById(id);
    }

    public List<Book> searchBook(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return bookRepository.findAll();
        }
        String lowerKeyword = keyword.toLowerCase();
        return bookRepository.findAll().stream()
                .filter(b -> b.getTitle().toLowerCase().contains(lowerKeyword) ||
                        b.getIsbn().contains(keyword) ||
                        (b.getAuthor() != null && b.getAuthor().getName().toLowerCase().contains(lowerKeyword)) ||
                        (b.getCategory() != null && b.getCategory().getName().toLowerCase().contains(lowerKeyword)))
                .collect(Collectors.toList());
    }

    // Stream API: lọc sách theo giá (từ -> đến)
    public List<Book> filterByPrice(BigDecimal from, BigDecimal to) {
        return bookRepository.findAll().stream()
                .filter(b -> (from == null || b.getPrice().compareTo(from) >= 0) &&
                        (to == null || b.getPrice().compareTo(to) <= 0))
                .collect(Collectors.toList());
    }

    // Stream API: nhóm theo category
    public Map<String, List<Book>> groupByCategory() {
        return bookRepository.findAll().stream()
                .filter(b -> b.getCategory() != null)
                .collect(Collectors.groupingBy(b -> b.getCategory().getName()));
    }

    // Stream API: top 5 sách bán chạy nhất (dựa trên salesCount)
    public List<Book> top5BestSellers() {
        return bookRepository.findAll().stream()
                .sorted((b1, b2) -> b2.getSalesCount().compareTo(b1.getSalesCount()))
                .limit(5)
                .collect(Collectors.toList());
    }
}