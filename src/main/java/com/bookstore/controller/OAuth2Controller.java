package com.bookstore.controller;

import com.bookstore.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/oauth2")
@RequiredArgsConstructor
@Tag(name = "OAuth2 Authentication", description = "OAuth2 login APIs")
@Slf4j
public class OAuth2Controller {

    @GetMapping("/providers")
    @Operation(summary = "Get available OAuth2 providers")
    public ApiResponse<Map<String, String>> getOAuth2Providers() {
        Map<String, String> providers = new HashMap<>();
        providers.put("google", "/oauth2/authorization/google");
        providers.put("facebook", "/oauth2/authorization/facebook");
        providers.put("github", "/oauth2/authorization/github");
        return ApiResponse.success(providers);
    }

    @GetMapping("/user")
    @Operation(summary = "Get current OAuth2 user")
    public ApiResponse<Map<String, Object>> getCurrentOAuth2User() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ApiResponse.error("User not authenticated");
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", authentication.getName());
        userInfo.put("authenticated", authentication.isAuthenticated());
        userInfo.put("authorities", authentication.getAuthorities());

        if (authentication.getPrincipal() instanceof com.bookstore.security.CustomOAuth2User) {
            com.bookstore.security.CustomOAuth2User oAuth2User =
                    (com.bookstore.security.CustomOAuth2User) authentication.getPrincipal();
            userInfo.put("id", oAuth2User.getId());
            userInfo.put("email", oAuth2User.getEmail());
            userInfo.put("fullName", oAuth2User.getFullName());
            userInfo.put("avatarUrl", oAuth2User.getAvatarUrl());
            userInfo.put("provider", oAuth2User.getProvider());
        }

        return ApiResponse.success(userInfo);
    }
}