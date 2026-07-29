package com.bookstore.controller;

import com.bookstore.dto.request.CheckoutRequest;
import com.bookstore.dto.response.ApiResponse;
import com.bookstore.dto.response.CheckoutResponse;
import com.bookstore.security.CustomUserDetails;
import com.bookstore.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
@Tag(name = "Checkout", description = "APIs for checkout and order processing")
@Slf4j
public class CheckoutController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Checkout - process cart to order")
    public ApiResponse<CheckoutResponse> checkout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CheckoutRequest request,
            HttpServletRequest httpServletRequest) {
        log.info("🛒 Checkout request for user: {}", userDetails.getUsername());
        CheckoutResponse response = orderService.checkout(userDetails.getId(), request, httpServletRequest);
        return ApiResponse.success("Checkout completed successfully", response);
    }
}