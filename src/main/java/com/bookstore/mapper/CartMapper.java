package com.bookstore.mapper;

import com.bookstore.dto.response.CartItemResponse;
import com.bookstore.dto.response.CartResponse;
import com.bookstore.model.Cart;
import com.bookstore.model.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CartMapper {
    // ============================================================
    // CartItem → CartItemResponse
    // ============================================================

    @Mapping(target = "bookId", source = "book.id")
    @Mapping(target = "bookTitle", source = "book.title")
    @Mapping(target = "bookIsbn", source = "book.isbn")
    @Mapping(target = "bookPrice", source = "book.price")
    @Mapping(target = "subtotal", expression = "java(calculateSubtotal(cartItem))")
    CartItemResponse toCartItemResponse(CartItem cartItem);

    List<CartItemResponse> toCartItemResponseList(List<CartItem> cartItems);

    // ============================================================
    // Cart → CartResponse
    // ============================================================

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "items", source = "cartItems")
    @Mapping(target = "totalItems", expression = "java(cart.getCartItems() != null ? cart.getCartItems().size() : 0)")
    @Mapping(target = "totalPrice", expression = "java(calculateTotalPrice(cart))")
    CartResponse toCartResponse(Cart cart);

    default BigDecimal calculateSubtotal(CartItem cartItem) {
        if (cartItem == null || cartItem.getBook() == null || cartItem.getBook().getPrice() == null) {
            return BigDecimal.ZERO;
        }
        return cartItem.getBook().getPrice()
                .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
    }

    default BigDecimal calculateTotalPrice(Cart cart) {
        if (cart == null || cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return cart.getCartItems().stream()
                .map(this::calculateSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
