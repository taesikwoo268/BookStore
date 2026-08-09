//package com.bookstore.integration;
//
//import com.bookstore.dto.request.LoginRequest;
//import com.bookstore.dto.request.RefreshTokenRequest;
//import com.bookstore.dto.request.RegisterRequest;
//import com.bookstore.dto.response.ApiResponse;
//import com.bookstore.dto.response.AuthResponse;
//import com.bookstore.dto.response.TokenRefreshResponse;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//import org.springframework.transaction.annotation.Transactional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@ActiveProfiles("test")
//@Transactional
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@DisplayName("Auth Flow Integration Tests")
//class AuthFlowIntegrationTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    private static String accessToken;
//    private static String refreshToken;
//    private static String username;
//    private static Long userId;
//
//    // ✅ Dùng timestamp để tạo username unique mỗi lần chạy
//    private static final String TEST_USERNAME = "testuser_" + System.currentTimeMillis();
//    private static final String TEST_EMAIL = "test_" + System.currentTimeMillis() + "@test.com";
//    private static final String TEST_PASSWORD = "Test@123456";
//
//    // ============================================================
//    // 1. REGISTER
//    // ============================================================
//
//    @Test
//    @Order(1)
//    @DisplayName("1. Register - should create new user")
//    void register_shouldCreateNewUser() throws Exception {
//        RegisterRequest request = RegisterRequest.builder()
//                .username(TEST_USERNAME)
//                .email(TEST_EMAIL)
//                .password(TEST_PASSWORD)
//                .fullName("Test User")
//                .build();
//
//        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andReturn();
//
//        String responseJson = result.getResponse().getContentAsString();
//        ApiResponse<AuthResponse> response = objectMapper.readValue(
//                responseJson,
//                new TypeReference<ApiResponse<AuthResponse>>() {}
//        );
//
//        assertThat(response.isSuccess()).isTrue();
//        assertThat(response.getMessage()).isEqualTo("User registered successfully");
//        assertThat(response.getData()).isNotNull();
//        assertThat(response.getData().getUsername()).isEqualTo(TEST_USERNAME);
//
//        // ✅ Lưu userId và username để dùng cho các test sau
//        userId = response.getData().getUserId();
//        username = response.getData().getUsername();
//
//        // ✅ Lưu token để dùng cho test sau
//        accessToken = response.getData().getAccessToken();
//        refreshToken = response.getData().getRefreshToken();
//
//        System.out.println("✅ [1] Register successful: userId=" + userId);
//    }
//
//    @Test
//    @Order(2)
//    @DisplayName("2. Register - duplicate username should fail (409)")
//    void register_duplicateUsername_shouldFail() throws Exception {
//        // ✅ Dùng username đã tồn tại từ test trước
//        RegisterRequest request = RegisterRequest.builder()
//                .username(TEST_USERNAME)  // Đã tồn tại
//                .email("different_" + System.currentTimeMillis() + "@test.com")
//                .password(TEST_PASSWORD)
//                .fullName("Duplicate User")
//                .build();
//
//        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isConflict())  // ✅ Kỳ vọng 409
//                .andReturn();
//
//        // Kiểm tra message
//        String responseJson = result.getResponse().getContentAsString();
//        ApiResponse<?> response = objectMapper.readValue(
//                responseJson,
//                new TypeReference<ApiResponse<?>>() {}
//        );
//        assertThat(response.isSuccess()).isFalse();
//        assertThat(response.getMessage()).contains("Username already exists");
//        System.out.println("✅ [2] Duplicate username correctly rejected");
//    }
//
//    // ============================================================
//    // 2. LOGIN
//    // ============================================================
//
//    @Test
//    @Order(3)
//    @DisplayName("3. Login - valid credentials should return tokens")
//    void login_validCredentials_shouldReturnTokens() throws Exception {
//        // ✅ Dùng username đã đăng ký
//        LoginRequest request = LoginRequest.builder()
//                .username(TEST_USERNAME)
//                .password(TEST_PASSWORD)
//                .build();
//
//        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andReturn();
//
//        String responseJson = result.getResponse().getContentAsString();
//        ApiResponse<AuthResponse> response = objectMapper.readValue(
//                responseJson,
//                new TypeReference<ApiResponse<AuthResponse>>() {}
//        );
//
//        assertThat(response.isSuccess()).isTrue();
//        assertThat(response.getData()).isNotNull();
//        assertThat(response.getData().getAccessToken()).isNotNull();
//
//        // ✅ Cập nhật token mới
//        accessToken = response.getData().getAccessToken();
//        refreshToken = response.getData().getRefreshToken();
//
//        System.out.println("✅ [3] Login successful");
//    }
//
//    // ============================================================
//    // 3. ACCESS PROTECTED ENDPOINT
//    // ============================================================
//
//    @Test
//    @Order(4)
//    @DisplayName("4. Protected endpoint - valid token should succeed (200)")
//    void protectedEndpoint_validToken_shouldSucceed() throws Exception {
//        assertThat(accessToken).as("Access token should not be null").isNotNull();
//
//        mockMvc.perform(get("/api/v1/books")
//                        .header("Authorization", "Bearer " + accessToken))
//                .andExpect(status().isOk());
//
//        System.out.println("✅ [4] Protected endpoint accessed with valid token");
//    }
//
//    @Test
//    @Order(5)
//    @DisplayName("5. Protected endpoint - invalid token should fail (403)")
//    void protectedEndpoint_invalidToken_shouldFail() throws Exception {
//        mockMvc.perform(get("/api/v1/books")
//                        .header("Authorization", "Bearer invalid_token_12345"))
//                .andExpect(status().isForbidden());  // ✅ Spring Security trả về 403
//    }
//
//    @Test
//    @Order(6)
//    @DisplayName("6. Protected endpoint - no token should fail (403)")
//    void protectedEndpoint_noToken_shouldFail() throws Exception {
//        mockMvc.perform(get("/api/v1/books"))
//                .andExpect(status().isForbidden());
//    }
//
//    // ============================================================
//    // 4. REFRESH TOKEN
//    // ============================================================
//
//    @Test
//    @Order(7)
//    @DisplayName("7. Refresh token - invalid token should fail (401)")
//    void refreshToken_invalidToken_shouldFail() throws Exception {
//        RefreshTokenRequest request = RefreshTokenRequest.builder()
//                .refreshToken("invalid_refresh_token")
//                .build();
//
//        mockMvc.perform(post("/api/v1/auth/refresh")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isUnauthorized());  // ✅ InvalidTokenException → 401
//    }
//}