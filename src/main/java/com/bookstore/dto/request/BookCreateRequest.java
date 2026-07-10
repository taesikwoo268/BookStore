package com.bookstore.dto.request;

import com.bookstore.validation.ValidISBN;
import jakarta.validation.constraints.*;
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
public class BookCreateRequest {

    @ValidISBN(message = "Invalid ISBN format")
    @NotBlank(message = "ISBN is required")
    private String isbn;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Price must not exceed 999999.99")
    private BigDecimal price;

    @Min(value = 0, message = "Stock must be non-negative")
    private Integer stock;

    @NotNull(message = "Author ID is required")
    private Long authorId;

    @NotEmpty(message = "At least one category ID is required")
    private List<Long> categoryIds;
}