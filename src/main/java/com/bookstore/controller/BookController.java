package com.bookstore.controller;

import com.bookstore.dto.request.BookCreateRequest;
import com.bookstore.dto.request.BookUpdateRequest;
import com.bookstore.dto.response.BookDetailResponse;
import com.bookstore.dto.response.BookResponse;
import com.bookstore.dto.response.BookSummaryResponse;
import com.bookstore.mapper.BookMapper;
import com.bookstore.model.Book;
import com.bookstore.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;
    private final BookMapper bookMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookResponse createBook(@Valid @RequestBody BookCreateRequest request) {
        Book book = bookService.createBook(request);
        return bookMapper.toResponse(book);
    }

    @PutMapping("/{id}")
    public BookResponse updateBook(@PathVariable Long id, @Valid @RequestBody BookUpdateRequest request) {
        Book book = bookService.updateBook(id, request);
        return bookMapper.toResponse(book);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
    }

    @GetMapping("/{id}")
    public BookDetailResponse getBookById(@PathVariable Long id) {
        Book book = bookService.getBookById(id);
        return bookMapper.toDetailResponse(book);
    }

    @GetMapping
    public List<BookSummaryResponse> getAllBooks() {
        return bookMapper.toSummaryList(bookService.getAllBooks());
    }

    @GetMapping("/search")
    public List<BookSummaryResponse> searchBooks(@RequestParam(required = false) String keyword) {
        return bookMapper.toSummaryList(bookService.searchBook(keyword));
    }

    @GetMapping("/filter/price")
    public List<BookSummaryResponse> filterByPrice(@RequestParam(required = false) BigDecimal from,
                                                   @RequestParam(required = false) BigDecimal to) {
        return bookMapper.toSummaryList(bookService.filterByPrice(from, to));
    }

    @GetMapping("/group-by-category")
    public Map<String, List<BookSummaryResponse>> groupByCategory() {
        return bookService.groupByCategory().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> bookMapper.toSummaryList(e.getValue())
                ));
    }

    @GetMapping("/top5")
    public List<BookSummaryResponse> getTop5BestSellers() {
        return bookMapper.toSummaryList(bookService.top5BestSellers());
    }
}