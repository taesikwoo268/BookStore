package com.bookstore.event;

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
public class OrderPlacedEvent {

    private String eventId;
    private String eventType;
    private LocalDateTime eventTime;

    private Long orderId;
    private String orderNumber;
    private Long userId;
    private String userEmail;

    private List<OrderItemEvent> items;
    private BigDecimal totalAmount;
    private String shippingAddress;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemEvent {
        private Long bookId;
        private String bookTitle;
        private String bookIsbn;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal subtotal;
    }
}