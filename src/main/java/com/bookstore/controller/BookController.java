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
import com.bookstore.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@Tag(name = "Book Management", description = "APIs for managing books")
public class BookController {

    private final BookService bookService;
    private final BookMapper bookMapper;

    // ============================================================
    // 1. GET ALL BOOKS (CACHED)
    // ============================================================

    @GetMapping
    @Operation(summary = "Get all books", description = "Retrieve all books (cached)")
    public ApiResponse<List<BookSummaryResponse>> getAllBooks() {
        List<BookSummaryResponse> books = bookService.getAllBooks();
        return ApiResponse.success(books);
    }

    // ============================================================
    // 2. CREATE BOOK
    // ============================================================

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
    @PreAuthorize("hasAuthority('book:write')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BookResponse> createBook(@Valid @RequestBody BookCreateRequest request) {
        BookResponse book = bookService.createBook(request);
        return ApiResponse.success("Book created successfully", book);
    }

    // ============================================================
    // 3. UPDATE BOOK
    // ============================================================

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
    @PreAuthorize("hasAuthority('book:write')")
    public ApiResponse<BookResponse> updateBook(
            @Parameter(description = "Book ID", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody BookUpdateRequest request) {
        BookResponse book = bookService.updateBook(id, request);
        return ApiResponse.success("Book updated successfully", book);
    }

    // ============================================================
    // 4. DELETE BOOK
    // ============================================================

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
    @PreAuthorize("hasAuthority('book:delete')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteBook(
            @Parameter(description = "Book ID", required = true, example = "1")
            @PathVariable Long id) {
        bookService.deleteBook(id);
        return ApiResponse.success("Book deleted successfully");
    }

    // ============================================================
    // 5. GET BOOK BY ID (CACHED)
    // ============================================================

    @Operation(summary = "Get book by ID", description = "Retrieve detailed information of a book by ID (cached)")
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
        BookDetailResponse book = bookService.getBookById(id);
        return ApiResponse.success(book);
    }

    // ============================================================
    // 6. GET BOOK BY ISBN (CACHED)
    // ============================================================

    @Operation(summary = "Get book by ISBN", description = "Retrieve detailed information of a book by ISBN (cached)")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Book found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Book not found")
    })
    @GetMapping("/isbn/{isbn}")
    public ApiResponse<BookDetailResponse> getBookByIsbn(
            @Parameter(description = "Book ISBN", required = true, example = "978-0439708184")
            @PathVariable String isbn) {
        BookDetailResponse book = bookService.getBookByIsbn(isbn);
        return ApiResponse.success(book);
    }

    // ============================================================
    // 7. GET BOOKS WITH FILTER (PAGINATION)
    // ============================================================

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
    @GetMapping("/filter")
    @PreAuthorize("isAuthenticated()")
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

        PageResponse<BookSummaryResponse> pageResult = bookService.getBooksWithFilter(filter);
        return ApiResponse.success("Books retrieved successfully", pageResult);
    }

    // ============================================================
    // 8. SEARCH BOOKS
    // ============================================================

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
        List<BookSummaryResponse> books = bookService.searchBook(keyword);
        return ApiResponse.success(books);
    }

    // ============================================================
    // 9. FILTER BY PRICE
    // ============================================================

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
        List<BookSummaryResponse> books = bookService.filterByPrice(from, to);
        return ApiResponse.success(books);
    }

    // ============================================================
    // 10. GROUP BY CATEGORY
    // ============================================================

    @Operation(summary = "Get books grouped by category", description = "Retrieve all books grouped by their category")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Grouping completed successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/group-by-category")
    public ApiResponse<Map<String, List<BookSummaryResponse>>> groupByCategory() {
        Map<String, List<BookSummaryResponse>> grouped = bookService.groupByCategory();
        return ApiResponse.success(grouped);
    }

    // ============================================================
    // 11. TOP 5 BEST SELLERS (CACHED)
    // ============================================================

    @Operation(summary = "Get top 5 best-selling books", description = "Retrieve the 5 books with highest sales (cached)")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Top 5 retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/top5")
    public ApiResponse<List<BookSummaryResponse>> getTop5BestSellers() {
        List<BookSummaryResponse> books = bookService.top5BestSellers();
        return ApiResponse.success(books);
    }

    // ============================================================
    // 12. CLEAR CACHE (Admin)
    // ============================================================

    @Operation(summary = "Clear all book caches", description = "Clear Redis cache for books (Admin only)")
    @DeleteMapping("/cache")
    @PreAuthorize("hasAuthority('book:delete')")
    public ApiResponse<Void> clearCache() {
        bookService.clearAllBookCache();
        return ApiResponse.success("All book caches cleared successfully");
    }
}