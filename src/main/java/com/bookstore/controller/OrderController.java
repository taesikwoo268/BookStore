package com.bookstore.controller;

import com.bookstore.dto.request.OrderRequest;
import com.bookstore.dto.response.ApiResponse;
import com.bookstore.model.Order;
import com.bookstore.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "APIs for managing orders")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Place an order")
    @PostMapping
    public ApiResponse<Order> placeOrder(@Valid @RequestBody OrderRequest request) {
        Order order = orderService.placeOrder(
                request.getBookId(),
                request.getUserId(),
                request.getQuantity()
        );
        return ApiResponse.success("Order placed successfully. Order ID: " + order.getId(), order);
    }
}