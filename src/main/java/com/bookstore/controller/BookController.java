package com.bookstore.controller;

import com.bookstore.model.Book;
import com.bookstore.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    @GetMapping("/get")
    public List<Book> getAll() {
        return bookService.getAllBooks();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Book addBook(@RequestBody Book book) {
        return bookService.addBook(book);
    }

    @PutMapping("/{id}")
    public Book updateBook(@PathVariable Long id, @RequestBody Book book) {
        return bookService.updateBook(id, book);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
    }

    @GetMapping("/search")
    public List<Book> search(@RequestParam(required = false) String keyword) {
        return bookService.searchBook(keyword);
    }

    @GetMapping("/filter/price")
    public List<Book> filterByPrice(@RequestParam(required = false) BigDecimal from,
                                    @RequestParam(required = false) BigDecimal to) {
        return bookService.filterByPrice(from, to);
    }

    @GetMapping("/group-by-category")
    public Map<String, List<Book>> groupByCategory() {
        return bookService.groupByCategory();
    }

    @GetMapping("/top5")
    public List<Book> top5BestSellers() {
        return bookService.top5BestSellers();
    }

    @GetMapping("/{id}")
    public Book getBookById(@PathVariable Long id) {
        return bookService.getBookById(id);
    }

    @GetMapping
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }
}