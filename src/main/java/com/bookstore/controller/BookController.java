package com.bookstore.controller;

import com.bookstore.dto.request.BookCreateRequest;
import com.bookstore.dto.request.BookUpdateRequest;
import com.bookstore.dto.response.ApiResponse;
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
    public ApiResponse<BookResponse> createBook(@Valid @RequestBody BookCreateRequest request) {
        Book book = bookService.createBook(request);
        return ApiResponse.success("Book created successfully", bookMapper.toResponse(book));
    }

    @PutMapping("/{id}")
    public ApiResponse<BookResponse> updateBook(@PathVariable Long id, @Valid @RequestBody BookUpdateRequest request) {
        Book book = bookService.updateBook(id, request);
        return ApiResponse.success("Book updated successfully", bookMapper.toResponse(book));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ApiResponse.success("Book deleted successfully");
    }

    @GetMapping("/{id}")
    public ApiResponse<BookDetailResponse> getBookById(@PathVariable Long id) {
        Book book = bookService.getBookById(id);
        return ApiResponse.success(bookMapper.toDetailResponse(book));
    }

    @GetMapping
    public ApiResponse<List<BookSummaryResponse>> getAllBooks() {
        List<BookSummaryResponse> books = bookMapper.toSummaryList(bookService.getAllBooks());
        return ApiResponse.success(books);
    }

    @GetMapping("/search")
    public ApiResponse<List<BookSummaryResponse>> searchBooks(@RequestParam(required = false) String keyword) {
        List<BookSummaryResponse> books = bookMapper.toSummaryList(bookService.searchBook(keyword));
        return ApiResponse.success(books);
    }

    @GetMapping("/filter/price")
    public ApiResponse<List<BookSummaryResponse>> filterByPrice(@RequestParam(required = false) BigDecimal from,
                                                                @RequestParam(required = false) BigDecimal to) {
        List<BookSummaryResponse> books = bookMapper.toSummaryList(bookService.filterByPrice(from, to));
        return ApiResponse.success(books);
    }

    @GetMapping("/group-by-category")
    public ApiResponse<Map<String, List<BookSummaryResponse>>> groupByCategory() {
        Map<String, List<BookSummaryResponse>> grouped = bookService.groupByCategory().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> bookMapper.toSummaryList(e.getValue())
                ));
        return ApiResponse.success(grouped);
    }

    @GetMapping("/top5")
    public ApiResponse<List<BookSummaryResponse>> getTop5BestSellers() {
        List<BookSummaryResponse> books = bookMapper.toSummaryList(bookService.top5BestSellers());
        return ApiResponse.success(books);
    }
}