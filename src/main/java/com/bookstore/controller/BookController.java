package com.bookstore.controller;

import com.bookstore.dto.request.BookCreateRequest;
import com.bookstore.dto.request.BookFilterRequest;
import com.bookstore.dto.request.BookUpdateRequest;
import com.bookstore.dto.response.ApiResponse;
import com.bookstore.dto.response.BookDetailResponse;
import com.bookstore.dto.response.BookResponse;
import com.bookstore.dto.response.BookSummaryResponse;
import com.bookstore.dto.response.PageResponse;
import com.bookstore.mapper.BookMapper;
import com.bookstore.model.Book;
import com.bookstore.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@Tag(name = "Book Management", description = "APIs for managing books")
public class BookController {

    private final BookService bookService;
    private final BookMapper bookMapper;

    // ===== CREATE =====
    @Operation(summary = "Create a new book", description = "Create a new book with the provided details")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Book created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "ISBN already exists")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BookResponse> createBook(@Valid @RequestBody BookCreateRequest request) {
        Book book = bookService.createBook(request);
        return ApiResponse.success("Book created successfully", bookMapper.toResponse(book));
    }

    // ===== UPDATE =====
    @Operation(summary = "Update an existing book", description = "Update book details by ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Book updated successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Book not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "ISBN already exists")
    })
    @PutMapping("/{id}")
    public ApiResponse<BookResponse> updateBook(
            @Parameter(description = "Book ID", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody BookUpdateRequest request) {
        Book book = bookService.updateBook(id, request);
        return ApiResponse.success("Book updated successfully", bookMapper.toResponse(book));
    }

    // ===== DELETE =====
    @Operation(summary = "Delete a book", description = "Delete book by ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "Book deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Book not found")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteBook(
            @Parameter(description = "Book ID", required = true, example = "1")
            @PathVariable Long id) {
        bookService.deleteBook(id);
        return ApiResponse.success("Book deleted successfully");
    }

    // ===== GET BY ID =====
    @Operation(summary = "Get book by ID", description = "Retrieve detailed information of a book by ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Book found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Book not found")
    })
    @GetMapping("/{id}")
    public ApiResponse<BookDetailResponse> getBookById(
            @Parameter(description = "Book ID", required = true, example = "1")
            @PathVariable Long id) {
        Book book = bookService.getBookById(id);
        return ApiResponse.success(bookMapper.toDetailResponse(book));
    }

    // ===== GET ALL WITH PAGINATION =====
    @Operation(
            summary = "Get all books with pagination and filters",
            description = "Retrieve a paginated list of books with optional filtering by genre and price range"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Books retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping
    public ApiResponse<PageResponse<BookSummaryResponse>> getBooksWithFilter(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of items per page", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Sort field and direction (e.g., price,asc or title,desc)",
                    example = "price,asc")
            @RequestParam(required = false) String sort,

            @Parameter(description = "Filter by genre/category name", example = "Fantasy")
            @RequestParam(required = false) String genre,

            @Parameter(description = "Minimum price filter", example = "10.00")
            @RequestParam(required = false) BigDecimal minPrice,

            @Parameter(description = "Maximum price filter", example = "50.00")
            @RequestParam(required = false) BigDecimal maxPrice) {

        BookFilterRequest filter = BookFilterRequest.builder()
                .page(page)
                .size(size)
                .sort(sort)
                .genre(genre)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .build();

        PageResponse<Book> pageResult = bookService.getBooksWithFilter(filter);

        PageResponse<BookSummaryResponse> pageResponse = PageResponse.<BookSummaryResponse>builder()
                .content(bookMapper.toSummaryList(pageResult.getContent()))
                .pageNumber(pageResult.getPageNumber())
                .pageSize(pageResult.getPageSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .first(pageResult.isFirst())
                .last(pageResult.isLast())
                .empty(pageResult.isEmpty())
                .numberOfElements(pageResult.getNumberOfElements())
                .build();

        return ApiResponse.success("Books retrieved successfully", pageResponse);
    }

    // ===== SEARCH =====
    @Operation(summary = "Search books", description = "Search books by keyword (title, ISBN, author, category)")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Search completed successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/search")
    public ApiResponse<List<BookSummaryResponse>> searchBooks(
            @Parameter(description = "Search keyword", example = "Harry Potter")
            @RequestParam(required = false) String keyword) {
        List<BookSummaryResponse> books = bookMapper.toSummaryList(bookService.searchBook(keyword));
        return ApiResponse.success(books);
    }

    // ===== FILTER BY PRICE =====
    @Operation(summary = "Filter books by price range", description = "Get books within specified price range")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Filter completed successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/filter/price")
    public ApiResponse<List<BookSummaryResponse>> filterByPrice(
            @Parameter(description = "Minimum price", example = "10.00")
            @RequestParam(required = false) BigDecimal from,

            @Parameter(description = "Maximum price", example = "50.00")
            @RequestParam(required = false) BigDecimal to) {
        List<BookSummaryResponse> books = bookMapper.toSummaryList(bookService.filterByPrice(from, to));
        return ApiResponse.success(books);
    }

    // ===== GROUP BY CATEGORY =====
    @Operation(summary = "Get books grouped by category", description = "Retrieve all books grouped by their category")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Grouping completed successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/group-by-category")
    public ApiResponse<Map<String, List<BookSummaryResponse>>> groupByCategory() {
        Map<String, List<BookSummaryResponse>> grouped = bookService.groupByCategory().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> bookMapper.toSummaryList(e.getValue())
                ));
        return ApiResponse.success(grouped);
    }

    // ===== TOP 5 BEST SELLERS =====
    @Operation(summary = "Get top 5 best-selling books", description = "Retrieve the 5 books with highest sales")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Top 5 retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/top5")
    public ApiResponse<List<BookSummaryResponse>> getTop5BestSellers() {
        List<BookSummaryResponse> books = bookMapper.toSummaryList(bookService.top5BestSellers());
        return ApiResponse.success(books);
    }
}