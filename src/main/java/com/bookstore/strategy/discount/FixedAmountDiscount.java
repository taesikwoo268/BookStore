package com.bookstore.strategy.discount;

import com.bookstore.model.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FixedAmountDiscount implements DiscountStrategy {

    private BigDecimal fixedAmount; // Số tiền cố định được giảm

    @Override
    public BigDecimal calculateDiscount(Order order) {
        if (!isApplicable(order)) {
            return BigDecimal.ZERO;
        }

        BigDecimal subtotal = order.getTotalAmount();
        // Giảm tối đa bằng subtotal (không âm)
        return fixedAmount.min(subtotal);
    }

    @Override
    public String getDescription() {
        return String.format("Giảm cố định $%s", fixedAmount);
    }
}