package com.bookstore.discount.strategy;

import com.bookstore.model.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PercentageDiscount implements DiscountStrategy {
    private BigDecimal percentage;

    @Override
    public BigDecimal calculateDiscount(Order order) {
        if(!isApplicable(order)) {
            return BigDecimal.ZERO;
        }
        BigDecimal subtotal = order.getTotalAmount();
        BigDecimal discount = subtotal.multiply(percentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return discount;
    }

    @Override
    public String getDescription() {
        return String.format("Giảm %s%% trên tổng đơn hàng", percentage);
    }
}
