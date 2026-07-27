package com.bookstore.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "books")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(nullable = false, length = 200)
    private String title;

    private BigDecimal price;

    private Integer stock;

    private Integer salesCount;

    // Many-to-One với Author
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Author author;

    // Many-to-Many với Category (cần tạo bảng trung gian)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "book_categories",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @Builder.Default
    private List<Category> categories = new ArrayList<>();

    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    @Builder.Default
    private List<CartItem> cartItems = new ArrayList<>();

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 0;

    public void reduceStock(int quantity) {
        if (stock == null || quantity > stock) {
            throw new IllegalStateException("Not enough stock. Available: " + stock + ", Requested: " + quantity);
        }
        this.stock -= quantity;
        this.salesCount = (this.salesCount != null ? this.salesCount : 0) + quantity;
    }

    public void increaseStock(int quantity) {
        this.stock = (this.stock != null ? this.stock : 0) + quantity;
    }
}