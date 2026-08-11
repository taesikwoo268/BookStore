package com.bookstore.strategy.discount;

import com.bookstore.model.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberDiscount implements DiscountStrategy {

    private String tier; // BRONZE, SILVER, GOLD, PLATINUM

    private static final Map<String, BigDecimal> TIER_DISCOUNTS = Map.of(
            "BRONZE", BigDecimal.valueOf(5),
            "SILVER", BigDecimal.valueOf(10),
            "GOLD", BigDecimal.valueOf(15),
            "PLATINUM", BigDecimal.valueOf(20)
    );

    @Override
    public BigDecimal calculateDiscount(Order order) {
        if (!isApplicable(order)) {
            return BigDecimal.ZERO;
        }

        BigDecimal discountPercent = TIER_DISCOUNTS.getOrDefault(tier.toUpperCase(), BigDecimal.ZERO);

        if (discountPercent.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal subtotal = order.getTotalAmount();
        BigDecimal discount = subtotal.multiply(discountPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return discount;
    }

    @Override
    public String getDescription() {
        return String.format("Giảm %s%% cho thành viên %s",
                TIER_DISCOUNTS.get(tier.toUpperCase()), tier);
    }

    @Override
    public boolean isApplicable(Order order) {
        return order != null && order.getUser() != null &&
                order.getOrderItems() != null && !order.getOrderItems().isEmpty();
    }
}