package com.bookstore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDetailResponse {
    private Long id;
    private String isbn;
    private String title;
    private BigDecimal price;
    private Integer stock;
    private Integer salesCount;
    private AuthorDetailResponse author;
    private CategoryDetailResponse category;
}