package com.bookstore.controller;

import com.bookstore.dto.request.BookCreateRequest;
import com.bookstore.dto.request.BookFilterRequest;
import com.bookstore.dto.response.*;
import com.bookstore.mapper.BookMapper;
import com.bookstore.security.JwtAuthenticationFilter;
import com.bookstore.security.JwtRsaProvider;
import com.bookstore.service.BookService;
import com.bookstore.service.RateLimiterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = BookController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        value = JwtAuthenticationFilter.class
                )
        }
)
@AutoConfigureMockMvc(addFilters = false)  // ✅ Tắt Security filters
@DisplayName("BookController Unit Tests")
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @MockBean
    private BookMapper bookMapper;

    @MockBean
    private RateLimiterService rateLimiterService;

    @MockBean
    private JwtRsaProvider jwtRsaProvider;

    private BookResponse bookResponse;
    private BookDetailResponse bookDetailResponse;
    private BookSummaryResponse bookSummaryResponse;
    private BookCreateRequest validRequest;
    private BookCreateRequest invalidRequest;

    @BeforeEach
    void setUp() {
        // ===== 1. BookResponse =====
        AuthorSummaryResponse authorSummary = AuthorSummaryResponse.builder()
                .id(1L)
                .name("J.K. Rowling")
                .build();

        CategorySummaryResponse categorySummary = CategorySummaryResponse.builder()
                .id(1L)
                .name("Fantasy")
                .build();

        bookResponse = BookResponse.builder()
                .id(1L)
                .isbn("978-0439708184")
                .title("Harry Potter and the Sorcerer's Stone")
                .price(new BigDecimal("19.99"))
                .stock(100)
                .salesCount(0)
                .author(authorSummary)
                .categories(List.of(categorySummary))
                .build();

        // ===== 2. BookDetailResponse =====
        AuthorDetailResponse authorDetail = AuthorDetailResponse.builder()
                .id(1L)
                .name("J.K. Rowling")
                .biography("British author")
                .build();

        CategoryDetailResponse categoryDetail = CategoryDetailResponse.builder()
                .id(1L)
                .name("Fantasy")
                .description("Magic books")
                .build();

        bookDetailResponse = BookDetailResponse.builder()
                .id(1L)
                .isbn("978-0439708184")
                .title("Harry Potter and the Sorcerer's Stone")
                .price(new BigDecimal("19.99"))
                .stock(100)
                .salesCount(0)
                .author(authorDetail)
                .categories(List.of(categoryDetail))
                .build();

        // ===== 3. BookSummaryResponse =====
        bookSummaryResponse = BookSummaryResponse.builder()
                .id(1L)
                .title("Harry Potter and the Sorcerer's Stone")
                .price(new BigDecimal("19.99"))
                .stock(100)
                .salesCount(0)
                .authorName("J.K. Rowling")
                .categoryNames(List.of("Fantasy"))
                .build();

        // ===== 4. Valid Request =====
        validRequest = BookCreateRequest.builder()
                .isbn("978-0439708184")
                .title("Test Book")
                .price(new BigDecimal("19.99"))
                .stock(100)
                .authorId(1L)
                .categoryIds(List.of(1L))
                .build();

        // ===== 5. Invalid Requests =====
        invalidRequest = BookCreateRequest.builder()
                .isbn("978-0439708184")
                .title(null)
                .price(new BigDecimal("19.99"))
                .stock(100)
                .authorId(1L)
                .categoryIds(List.of(1L))
                .build();

        // ===== 6. Mock RateLimiter =====
        when(rateLimiterService.isAllowed(any())).thenReturn(true);
        when(rateLimiterService.getRemainingRequests(any())).thenReturn(30L);
        when(rateLimiterService.getResetTime(any())).thenReturn(60L);
    }

    // ============================================================
    // GET TESTS
    // ============================================================

    @Test
    @DisplayName("GET /books/filter - returns paginated books")
    void getBooksWithFilter_returnsPaginatedBooks() throws Exception {
        PageResponse<BookSummaryResponse> pageResponse = PageResponse.<BookSummaryResponse>builder()
                .content(List.of(bookSummaryResponse))
                .pageNumber(0)
                .pageSize(20)
                .totalElements(1)
                .totalPages(1)
                .first(true)
                .last(true)
                .empty(false)
                .numberOfElements(1)
                .build();

        when(bookService.getBooksWithFilter(any(BookFilterRequest.class))).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/books/filter")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /books - returns empty list when no books")
    void getBooksWithFilter_returnsEmptyList() throws Exception {
        PageResponse<BookSummaryResponse> emptyPage = PageResponse.<BookSummaryResponse>builder()
                .content(List.of())
                .pageNumber(0)
                .pageSize(20)
                .totalElements(0)
                .totalPages(0)
                .first(true)
                .last(true)
                .empty(true)
                .numberOfElements(0)
                .build();

        when(bookService.getBooksWithFilter(any(BookFilterRequest.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/v1/books/filter")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /books/{id} - returns book")
    void getBookById_returnsBook() throws Exception {
        when(bookService.getBookById(anyLong())).thenReturn(bookDetailResponse);

        mockMvc.perform(get("/api/v1/books/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // ============================================================
    // POST TESTS - VALIDATION
    // ============================================================

    @Test
    @DisplayName("POST /books - valid request returns 201")
    @WithMockUser(username = "admin", authorities = {"book:write"})
    void createBook_validRequest_returns201() throws Exception {
        when(bookService.createBook(any(BookCreateRequest.class))).thenReturn(bookResponse);

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /books - missing title returns 400")
    @WithMockUser(username = "admin", authorities = {"book:write"})
    void createBook_missingTitle_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /books - missing ISBN returns 400")
    @WithMockUser(username = "admin", authorities = {"book:write"})
    void createBook_missingIsbn_returns400() throws Exception {
        BookCreateRequest request = BookCreateRequest.builder()
                .isbn(null)
                .title("Test Book")
                .price(new BigDecimal("19.99"))
                .stock(100)
                .authorId(1L)
                .categoryIds(List.of(1L))
                .build();

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /books - negative price returns 400")
    @WithMockUser(username = "admin", authorities = {"book:write"})
    void createBook_negativePrice_returns400() throws Exception {
        BookCreateRequest request = BookCreateRequest.builder()
                .isbn("978-1234567890")
                .title("Test Book")
                .price(new BigDecimal("-19.99"))
                .stock(100)
                .authorId(1L)
                .categoryIds(List.of(1L))
                .build();

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /books - empty categories returns 400")
    @WithMockUser(username = "admin", authorities = {"book:write"})
    void createBook_emptyCategories_returns400() throws Exception {
        BookCreateRequest request = BookCreateRequest.builder()
                .isbn("978-1234567890")
                .title("Test Book")
                .price(new BigDecimal("19.99"))
                .stock(100)
                .authorId(1L)
                .categoryIds(List.of())
                .build();

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /books - missing authorId returns 400")
    @WithMockUser(username = "admin", authorities = {"book:write"})
    void createBook_missingAuthorId_returns400() throws Exception {
        BookCreateRequest request = BookCreateRequest.builder()
                .isbn("978-1234567890")
                .title("Test Book")
                .price(new BigDecimal("19.99"))
                .stock(100)
                .authorId(null)
                .categoryIds(List.of(1L))
                .build();

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}