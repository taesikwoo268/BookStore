package com.bookstore.service;

import com.bookstore.exception.BookNotFoundException;
import com.bookstore.exception.DuplicateISBNException;
import com.bookstore.model.Author;
import com.bookstore.model.Book;
import com.bookstore.repository.BookRepository;
import com.bookstore.service.BookValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getBookById(Long id) {
        return bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));
    }

    public Book addBook(Book book) {
        BookValidator.validate(book);
        if (bookRepository.existsByIsbn(book.getIsbn())) {
            throw new DuplicateISBNException("Book with ISBN " + book.getIsbn() + " already exists");
        }
        return bookRepository.save(book);
    }

    public Book updateBook(Long id, Book updatedBook) {
        Book existing = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));
        BookValidator.validate(updatedBook);
        // Kiểm tra ISBN trùng (trừ chính nó)
        if (!existing.getIsbn().equals(updatedBook.getIsbn()) &&
                bookRepository.existsByIsbn(updatedBook.getIsbn())) {
            throw new DuplicateISBNException("ISBN " + updatedBook.getIsbn() + " already used by another book");
        }
        updatedBook.setId(id);
        return bookRepository.save(updatedBook);
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
                        (b.getAuthors() != null && b.getAuthors().stream().map(Author::getName).anyMatch(name -> name.toLowerCase().contains(lowerKeyword))) ||
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