package com.bookstore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResponse {
    private Long orderId;
    private String orderNumber;
    private Long userId;
    private String status;
    private BigDecimal totalAmount;
    private List<OrderItemResponse> items;
    private PaymentResponse payment;
    private String shippingAddress;
    private String billingAddress;
    private LocalDateTime orderDate;
    private String message;
}