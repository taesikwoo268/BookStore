package com.bookstore.discount;

import com.bookstore.discount.strategy.*;
import com.bookstore.model.Order;
import com.bookstore.model.OrderItem;
import com.bookstore.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiscountStrategyTest {

    private Order order;

    @BeforeEach
    void setUp() {
        User user = User.builder().id(1L).username("testuser").build();
        order = Order.builder()
                .user(user)
                .totalAmount(new BigDecimal("100.00"))
                .build();

        OrderItem item1 = OrderItem.builder()
                .book(null) // mock book
                .quantity(3)
                .price(new BigDecimal("20.00"))
                .build();
        OrderItem item2 = OrderItem.builder()
                .book(null)
                .quantity(2)
                .price(new BigDecimal("20.00"))
                .build();
        order.setOrderItems(List.of(item1, item2));
    }

    @Test
    @DisplayName("PercentageDiscount - 10% off")
    void testPercentageDiscount() {
        DiscountStrategy strategy = new PercentageDiscount(BigDecimal.valueOf(10));
        BigDecimal discount = strategy.calculateDiscount(order);

        assertThat(discount).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(strategy.getDescription()).contains("10%");
    }

    @Test
    @DisplayName("FixedAmountDiscount - $5 off")
    void testFixedAmountDiscount() {
        DiscountStrategy strategy = new FixedAmountDiscount(BigDecimal.valueOf(5));
        BigDecimal discount = strategy.calculateDiscount(order);

        assertThat(discount).isEqualByComparingTo(new BigDecimal("5.00"));
    }

    @Test
    @DisplayName("BuyXGetYDiscount - Buy 2 Get 1 Free (50% off)")
    void testBuyXGetYDiscount() {
        // 3 items: Buy 2 Get 1 Free → 1 free item → 50% off $20 = $10
        DiscountStrategy strategy = new BuyXGetYDiscount(2, 1, BigDecimal.valueOf(50));
        BigDecimal discount = strategy.calculateDiscount(order);

        assertThat(discount).isEqualByComparingTo(new BigDecimal("10.00"));
    }

    @Test
    @DisplayName("MemberDiscount - SILVER tier 10% off")
    void testMemberDiscount() {
        DiscountStrategy strategy = new MemberDiscount("SILVER");
        BigDecimal discount = strategy.calculateDiscount(order);

        assertThat(discount).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(strategy.getDescription()).contains("SILVER");
    }

    @Test
    @DisplayName("MemberDiscount - PLATINUM tier 20% off")
    void testMemberDiscountPlatinum() {
        DiscountStrategy strategy = new MemberDiscount("PLATINUM");
        BigDecimal discount = strategy.calculateDiscount(order);

        assertThat(discount).isEqualByComparingTo(new BigDecimal("20.00"));
    }
}