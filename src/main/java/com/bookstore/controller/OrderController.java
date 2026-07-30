package com.bookstore.controller;

import com.bookstore.dto.response.ApiResponse;
import com.bookstore.model.Order;
import com.bookstore.enums.OrderStatus;
import com.bookstore.service.OrderStateMachineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "Admin APIs for order management")
@Slf4j
public class OrderController {

    private final OrderStateMachineService orderStateMachineService;

    /**
     * Xác nhận đơn hàng: PENDING → CONFIRMED
     */
    @PutMapping("/{orderId}/confirm")
    @PreAuthorize("hasAuthority('UPDATE_ORDER')")
    @Operation(summary = "Confirm order (PENDING → CONFIRMED)")
    public ApiResponse<Order> confirmOrder(@PathVariable Long orderId) {
        log.info("📋 Confirming order: {}", orderId);
        Order order = orderStateMachineService.confirmOrder(orderId);
        return ApiResponse.success("Order confirmed successfully", order);
    }

    /**
     * Bắt đầu xử lý đơn hàng: CONFIRMED → PROCESSING
     */
    @PutMapping("/{orderId}/process")
    @PreAuthorize("hasAuthority('UPDATE_ORDER')")
    @Operation(summary = "Process order (CONFIRMED → PROCESSING)")
    public ApiResponse<Order> processOrder(@PathVariable Long orderId) {
        log.info("⚙️ Processing order: {}", orderId);
        Order order = orderStateMachineService.transitionTo(
                orderId,
                OrderStatus.PROCESSING,
                "Start processing"
        );
        return ApiResponse.success("Order processing started", order);
    }

    /**
     * Giao hàng: PROCESSING → SHIPPED
     */
    @PutMapping("/{orderId}/ship")
    @PreAuthorize("hasAuthority('UPDATE_ORDER')")
    @Operation(summary = "Ship order (PROCESSING → SHIPPED)")
    public ApiResponse<Order> shipOrder(@PathVariable Long orderId) {
        log.info("🚚 Shipping order: {}", orderId);
        Order order = orderStateMachineService.transitionTo(
                orderId,
                OrderStatus.SHIPPED,
                "Order shipped"
        );
        return ApiResponse.success("Order shipped successfully", order);
    }

    /**
     * Xác nhận đã nhận hàng: SHIPPED → DELIVERED
     */
    @PutMapping("/{orderId}/deliver")
    @PreAuthorize("hasAuthority('UPDATE_ORDER')")
    @Operation(summary = "Deliver order (SHIPPED → DELIVERED)")
    public ApiResponse<Order> deliverOrder(@PathVariable Long orderId) {
        log.info("📦 Delivering order: {}", orderId);
        Order order = orderStateMachineService.deliverOrder(orderId);
        return ApiResponse.success("Order delivered successfully", order);
    }

    /**
     * Hủy đơn hàng: PENDING/CONFIRMED/PROCESSING → CANCELLED
     */
    @PutMapping("/{orderId}/cancel")
    @PreAuthorize("hasAnyAuthority('UPDATE_ORDER', 'DELETE_ORDER', 'CANCEL_ORDER')")
    @Operation(summary = "Cancel order (PENDING/CONFIRMED → CANCELLED)")
    public ApiResponse<Order> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason) {
        log.info("❌ Cancelling order: {}", orderId);
        String cancelReason = reason != null ? reason : "Cancelled by admin";
        Order order = orderStateMachineService.cancelOrder(orderId, cancelReason);
        return ApiResponse.success("Order cancelled successfully", order);
    }

    /**
     * Kiểm tra các trạng thái có thể chuyển từ trạng thái hiện tại
     */
    @GetMapping("/{orderId}/possible-transitions")
    @PreAuthorize("hasAuthority('READ_ORDER')")
    @Operation(summary = "Get possible transitions for order")
    public ApiResponse<Object> getPossibleTransitions(@PathVariable Long orderId) {
        // Có thể thêm logic để lấy order và kiểm tra
        return ApiResponse.success("Check possible transitions", null);
    }
}