package com.bookstore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private Long bookId;
    private String bookTitle;
    private String bookIsbn;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
}