package com.bookstore.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookFilterRequest {
    private Integer page;
    private Integer size;
    private String sort;
    private String genre;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}