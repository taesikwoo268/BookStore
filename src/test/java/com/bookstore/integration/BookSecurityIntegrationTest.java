package com.bookstore.integration;

import com.bookstore.dto.request.BookCreateRequest;
import com.bookstore.dto.response.ApiResponse;
import com.bookstore.dto.response.AuthResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String adminToken;
    private static Long bookId;

    @BeforeEach
    void setUp() throws Exception {
        // ✅ Chỉ login 1 lần duy nhất
        if (adminToken == null) {
            adminToken = login("admin", "123456");
        }

        // ✅ Tạo book test nếu chưa có (dùng ISBN hợp lệ)
        if (bookId == null) {
            bookId = createTestBook();
        }
    }

    // ============================================================
    // HELPER: Login
    // ============================================================

    private String login(String username, String password) throws Exception {
        String loginRequest = String.format(
                "{\"username\":\"%s\",\"password\":\"%s\"}",
                username, password
        );

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();

        ApiResponse<AuthResponse> apiResponse = objectMapper.readValue(
                response,
                new TypeReference<ApiResponse<AuthResponse>>() {}
        );

        return apiResponse.getData().getAccessToken();
    }

    // ============================================================
    // HELPER: Tạo book test (dùng ISBN hợp lệ)
    // ============================================================

    private Long createTestBook() throws Exception {
        // ✅ Dùng ISBN hợp lệ (không bị validation)
        BookCreateRequest request = BookCreateRequest.builder()
                .isbn("978-604-123-456-7")  // ✅ ISBN hợp lệ
                .title("Test Security Book")
                .price(new BigDecimal("19.99"))
                .stock(100)
                .authorId(1L)
                .categoryIds(List.of(1L))
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/books")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        ApiResponse<com.bookstore.dto.response.BookResponse> apiResponse = objectMapper.readValue(
                response,
                new TypeReference<ApiResponse<com.bookstore.dto.response.BookResponse>>() {}
        );

        return apiResponse.getData().getId();
    }

    // ============================================================
    // TEST: UNAUTHORIZED (401)
    // ============================================================

    @Test
    @Order(1)
    void getBooks_WithoutToken_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(2)
    void createBook_WithoutToken_ShouldReturn401() throws Exception {
        BookCreateRequest request = BookCreateRequest.builder()
                .isbn("978-604-123-456-7")
                .title("Unauthorized Book")
                .price(new BigDecimal("19.99"))
                .stock(100)
                .authorId(1L)
                .categoryIds(List.of(1L))
                .build();

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(3)
    void deleteBook_WithoutToken_ShouldReturn401() throws Exception {
        mockMvc.perform(delete("/api/v1/books/{id}", 999L))
                .andExpect(status().isUnauthorized());
    }

    // ============================================================
    // TEST: VALID TOKEN (200/201/204)
    // ============================================================

    @Test
    @Order(4)
    void getBooks_WithAdminToken_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/v1/books")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @Order(5)
    void getBookById_WithAdminToken_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/v1/books/{id}", bookId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @Order(6)
    void createBook_WithAdminToken_ShouldReturn201() throws Exception {
        BookCreateRequest request = BookCreateRequest.builder()
                .isbn("978-604-123-456-8")  // ✅ ISBN hợp lệ, khác với book test
                .title("Valid Token Book")
                .price(new BigDecimal("19.99"))
                .stock(100)
                .authorId(1L)
                .categoryIds(List.of(1L))
                .build();

        mockMvc.perform(post("/api/v1/books")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Valid Token Book"));
    }

    @Test
    @Order(7)
    void updateBook_WithAdminToken_ShouldReturn200() throws Exception {
        BookCreateRequest request = BookCreateRequest.builder()
                .isbn("978-604-123-456-8")
                .title("Updated Valid Book")
                .price(new BigDecimal("29.99"))
                .stock(50)
                .authorId(1L)
                .categoryIds(List.of(1L))
                .build();

        mockMvc.perform(put("/api/v1/books/{id}", bookId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Updated Valid Book"));
    }

    @Test
    @Order(8)
    void deleteBook_WithAdminToken_ShouldReturn204() throws Exception {
        // Tạo sách mới để xóa (tránh xóa sách dùng chung)
        BookCreateRequest request = BookCreateRequest.builder()
                .isbn("978-604-123-456-9")
                .title("Book To Delete")
                .price(new BigDecimal("19.99"))
                .stock(100)
                .authorId(1L)
                .categoryIds(List.of(1L))
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/books")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        ApiResponse<com.bookstore.dto.response.BookResponse> apiResponse = objectMapper.readValue(
                response,
                new TypeReference<ApiResponse<com.bookstore.dto.response.BookResponse>>() {}
        );
        Long deleteBookId = apiResponse.getData().getId();

        mockMvc.perform(delete("/api/v1/books/{id}", deleteBookId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }
}