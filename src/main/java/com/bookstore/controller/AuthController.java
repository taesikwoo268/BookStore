package com.bookstore.controller;

import com.bookstore.dto.request.LoginRequest;
import com.bookstore.dto.request.RefreshTokenRequest;
import com.bookstore.dto.request.RegisterRequest;
import com.bookstore.dto.response.ApiResponse;
import com.bookstore.dto.response.AuthResponse;
import com.bookstore.dto.response.TokenRefreshResponse;
import com.bookstore.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register new user")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ApiResponse.success("User registered successfully", response);
    }

    @PostMapping("/login")
    @Operation(summary = "Login user")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        AuthResponse response = authService.login(request, httpRequest);
        return ApiResponse.success("Login successful", response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ApiResponse<TokenRefreshResponse> refresh(@Valid @RequestBody RefreshTokenRequest request,HttpServletRequest httpServletRequest) {
        TokenRefreshResponse response = authService.refreshToken(request,httpServletRequest);
        return ApiResponse.success("Token refreshed successfully", response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user")
    public ApiResponse<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String refreshToken = null;
        if (authorization != null && authorization.startsWith("Bearer ")) {
            // Trong logout, client gửi refresh token trong header
            refreshToken = authorization.substring(7);
        }
        authService.logout(refreshToken);
        return ApiResponse.success("Logout successful");
    }
}