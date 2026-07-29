package com.bookstore.controller;

import com.bookstore.dto.request.BookCreateRequest;
import com.bookstore.dto.response.ApiResponse;
import com.bookstore.dto.response.BookResponse;
import com.bookstore.dto.response.BookSummaryResponse;
import com.bookstore.dto.response.PageResponse;
import com.bookstore.mapper.BookMapper;
import com.bookstore.model.Author;
import com.bookstore.model.Book;
import com.bookstore.model.Category;
import com.bookstore.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "admin", authorities = {"book:write"})
@DisplayName("BookController Integration Tests")
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @MockBean
    private BookMapper bookMapper;

    private Book book;
    private BookResponse bookResponse;
    private BookSummaryResponse bookSummaryResponse;

    @BeforeEach
    void setUp() {
        // ===== Book =====
        Author author = Author.builder()
                .id(1L)
                .name("J.K. Rowling")
                .biography("British author")
                .build();

        Category category = Category.builder()
                .id(1L)
                .name("Fantasy")
                .description("Magic books")
                .build();

        book = Book.builder()
                .id(1L)
                .isbn("978-0439708184")  // ✅ ISBN-13 hợp lệ
                .title("Harry Potter")
                .price(new BigDecimal("19.99"))
                .stock(100)
                .salesCount(0)
                .author(author)
                .categories(List.of(category))
                .version(0)
                .build();

        // ===== BookResponse =====
        bookResponse = BookResponse.builder()
                .id(1L)
                .isbn("978-0439708184")  // ✅ ISBN-13 hợp lệ
                .title("Harry Potter")
                .price(new BigDecimal("19.99"))
                .stock(100)
                .salesCount(0)
                .build();

        // ===== BookSummaryResponse =====
        bookSummaryResponse = BookSummaryResponse.builder()
                .id(1L)
                .isbn("978-0439708184")  // ✅ ISBN-13 hợp lệ
                .title("Harry Potter")
                .price(new BigDecimal("19.99"))
                .stock(100)
                .salesCount(0)
                .authorName("J.K. Rowling")
                .categoryNames(List.of("Fantasy"))
                .build();
    }

    // ============================================================
    // 1. GET /books - PAGINATION
    // ============================================================

    @Test
    @DisplayName("GET /books - returns list of books (không phân trang)")
    void getBooks_returnsListOfBooks() throws Exception {
        // Given
        when(bookService.getAllBooks()).thenReturn(List.of(book));
        when(bookMapper.toResponseList(any())).thenReturn(List.of(bookResponse));

        // When & Then
        mockMvc.perform(get("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())  // ✅ data là Array
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].title").value("Harry Potter"));
    }

    @Test
    @DisplayName("GET /books - returns empty list when no books")
    void getBooks_noBooks_returnsEmptyList() throws Exception {
        // Given
        when(bookService.getAllBooks()).thenReturn(List.of());
        when(bookMapper.toResponseList(any())).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("GET /books - with filter parameters (giữ nguyên test cũ)")
    void getBooks_withFilters_returnsList() throws Exception {
        // Given
        when(bookService.filterByPrice(any(), any())).thenReturn(List.of(book));
        when(bookMapper.toSummaryList(any())).thenReturn(List.of(bookSummaryResponse));

        // When & Then
        mockMvc.perform(get("/api/v1/books/filter/price")
                        .param("from", "10")
                        .param("to", "50")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    // ============================================================
    // 2. GET /books - CÁC ENDPOINT KHÁC
    // ============================================================

    @Test
    @DisplayName("GET /books/search - returns matching books")
    void searchBooks_returnsMatchingBooks() throws Exception {
        // Given
        when(bookService.searchBook(any())).thenReturn(List.of(book));
        when(bookMapper.toSummaryList(any())).thenReturn(List.of(bookSummaryResponse));

        // When & Then
        mockMvc.perform(get("/api/v1/books/search")
                        .param("keyword", "Harry")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /books/top5 - returns top 5 best sellers")
    void getTop5BestSellers_returnsList() throws Exception {
        // Given
        when(bookService.top5BestSellers()).thenReturn(List.of(book));
        when(bookMapper.toSummaryList(any())).thenReturn(List.of(bookSummaryResponse));

        // When & Then
        mockMvc.perform(get("/api/v1/books/top5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /books/{id} - returns book by id")
    void getBookById_returnsBook() throws Exception {
        // Given
        when(bookService.getBookById(anyLong())).thenReturn(book);
        when(bookMapper.toResponse(any())).thenReturn(bookResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/books/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.title").value("Harry Potter"));
    }

    // ============================================================
    // 3. POST /books - VALIDATION ERRORS (400)
    // ============================================================

    @Test
    @DisplayName("POST /books - valid request returns 201")
    void createBook_validRequest_returns201() throws Exception {
        // Given
        BookCreateRequest request = BookCreateRequest.builder()
                .isbn("978-0439708184")  // ✅ ISBN hợp lệ
                .title("Valid Book")
                .price(new BigDecimal("19.99"))
                .stock(100)
                .authorId(1L)
                .categoryIds(List.of(1L))
                .build();

        when(bookService.createBook(any())).thenReturn(book);
        when(bookMapper.toResponse(any())).thenReturn(bookResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Book created successfully"))
                .andExpect(jsonPath("$.data.title").value("Harry Potter"));
    }

    @Test
    @DisplayName("POST /books - empty ISBN returns 400")
    void createBook_emptyISBN_returns400() throws Exception {
        // Given
        BookCreateRequest request = BookCreateRequest.builder()
                .isbn("")  // ❌ Empty ISBN
                .title("Valid Book")
                .price(new BigDecimal("19.99"))
                .stock(100)
                .authorId(1L)
                .categoryIds(List.of(1L))
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @DisplayName("POST /books - null ISBN returns 400")
    void createBook_nullISBN_returns400() throws Exception {
        // Given
        BookCreateRequest request = BookCreateRequest.builder()
                .isbn(null)  // ❌ Null ISBN
                .title("Valid Book")
                .price(new BigDecimal("19.99"))
                .stock(100)
                .authorId(1L)
                .categoryIds(List.of(1L))
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @DisplayName("POST /books - empty title returns 400")
    void createBook_emptyTitle_returns400() throws Exception {
        // Given
        BookCreateRequest request = BookCreateRequest.builder()
                .isbn("978-0439708184")
                .title("")  // ❌ Empty Title
                .price(new BigDecimal("19.99"))
                .stock(100)
                .authorId(1L)
                .categoryIds(List.of(1L))
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[?(@.contains('Title'))]").exists());
    }

    @Test
    @DisplayName("POST /books - null title returns 400")
    void createBook_nullTitle_returns400() throws Exception {
        // Given
        BookCreateRequest request = BookCreateRequest.builder()
                .isbn("978-0439708184")
                .title(null)  // ❌ Null Title
                .price(new BigDecimal("19.99"))
                .stock(100)
                .authorId(1L)
                .categoryIds(List.of(1L))
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @DisplayName("POST /books - negative price returns 400")
    void createBook_negativePrice_returns400() throws Exception {
        // Given
        BookCreateRequest request = BookCreateRequest.builder()
                .isbn("978-0439708184")
                .title("Valid Book")
                .price(new BigDecimal("-19.99"))  // ❌ Negative Price
                .stock(100)
                .authorId(1L)
                .categoryIds(List.of(1L))
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[?(@.contains('Price'))]").exists());
    }

    @Test
    @DisplayName("POST /books - null price returns 400")
    void createBook_nullPrice_returns400() throws Exception {
        // Given
        BookCreateRequest request = BookCreateRequest.builder()
                .isbn("978-0439708184")
                .title("Valid Book")
                .price(null)  // ❌ Null Price
                .stock(100)
                .authorId(1L)
                .categoryIds(List.of(1L))
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @DisplayName("POST /books - negative stock returns 400")
    void createBook_negativeStock_returns400() throws Exception {
        // Given
        BookCreateRequest request = BookCreateRequest.builder()
                .isbn("978-0439708184")
                .title("Valid Book")
                .price(new BigDecimal("19.99"))
                .stock(-5)  // ❌ Negative Stock
                .authorId(1L)
                .categoryIds(List.of(1L))
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[?(@.contains('stock'))]").exists());
    }

    @Test
    @DisplayName("POST /books - null authorId returns 400")
    void createBook_nullAuthorId_returns400() throws Exception {
        // Given
        BookCreateRequest request = BookCreateRequest.builder()
                .isbn("978-0439708184")
                .title("Valid Book")
                .price(new BigDecimal("19.99"))
                .stock(100)
                .authorId(null)  // ❌ Null AuthorId
                .categoryIds(List.of(1L))
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[?(@.contains('Author'))]").exists());
    }

    @Test
    @DisplayName("POST /books - null categoryIds returns 400")
    void createBook_nullCategoryIds_returns400() throws Exception {
        // Given
        BookCreateRequest request = BookCreateRequest.builder()
                .isbn("978-0439708184")
                .title("Valid Book")
                .price(new BigDecimal("19.99"))
                .stock(100)
                .authorId(1L)
                .categoryIds(null)  // ❌ Null CategoryIds
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[?(@.contains('category'))]").exists());
    }

    @Test
    @DisplayName("POST /books - empty categoryIds returns 400")
    void createBook_emptyCategoryIds_returns400() throws Exception {
        // Given
        BookCreateRequest request = BookCreateRequest.builder()
                .isbn("978-0439708184")
                .title("Valid Book")
                .price(new BigDecimal("19.99"))
                .stock(100)
                .authorId(1L)
                .categoryIds(List.of())  // ❌ Empty CategoryIds
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[?(@.contains('category'))]").exists());
    }

    @Test
    @DisplayName("POST /books - multiple validation errors returns 400")
    void createBook_multipleValidationErrors_returns400() throws Exception {
        // Given
        BookCreateRequest request = BookCreateRequest.builder()
                .isbn("")  // ❌ Empty ISBN
                .title("")  // ❌ Empty Title
                .price(new BigDecimal("-19.99"))  // ❌ Negative Price
                .stock(-5)  // ❌ Negative Stock
                .authorId(null)  // ❌ Null AuthorId
                .categoryIds(List.of())  // ❌ Empty CategoryIds
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()").value(org.hamcrest.Matchers.greaterThan(1)));
    }

    @Test
    @DisplayName("POST /books - empty request body returns 400")
    void createBook_emptyRequestBody_returns400() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors").isArray());
    }
}