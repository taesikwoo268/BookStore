package com.bookstore.controller;

import com.bookstore.dto.request.AddToCartRequest;
import com.bookstore.dto.request.UpdateCartItemRequest;
import com.bookstore.dto.response.ApiResponse;
import com.bookstore.dto.response.CartResponse;
import com.bookstore.security.CustomUserDetails;
import com.bookstore.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Cart Management", description = "APIs for managing shopping cart")
@Slf4j
public class CartController {

    private final CartService cartService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user's cart")
    public ApiResponse<CartResponse> getCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        CartResponse response = cartService.getCart(userDetails.getId());
        return ApiResponse.success("Cart retrieved successfully", response);
    }

    @PostMapping("/items")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add item to cart")
    public ApiResponse<CartResponse> addToCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AddToCartRequest request) {
        CartResponse response = cartService.addToCart(userDetails.getId(), request);
        return ApiResponse.success("Item added to cart successfully", response);
    }

    @PutMapping("/items/{itemId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update cart item quantity")
    public ApiResponse<CartResponse> updateCartItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        CartResponse response = cartService.updateCartItem(userDetails.getId(), itemId, request);
        return ApiResponse.success("Cart item updated successfully", response);
    }

    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove item from cart")
    public ApiResponse<CartResponse> deleteCartItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long itemId) {
        CartResponse response = cartService.deleteCartItem(userDetails.getId(), itemId);
        return ApiResponse.success("Item removed from cart successfully", response);
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Clear all items from cart")
    public ApiResponse<CartResponse> clearCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        CartResponse response = cartService.clearCart(userDetails.getId());
        return ApiResponse.success("Cart cleared successfully", response);
    }
}