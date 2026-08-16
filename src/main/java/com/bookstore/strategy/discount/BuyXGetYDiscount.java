package com.bookstore.strategy.discount;

import com.bookstore.model.Order;
import com.bookstore.model.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuyXGetYDiscount implements DiscountStrategy {

    private int x; // Số lượng mua
    private int y; // Số lượng được tặng
    private BigDecimal discountPercentage; // % giảm cho sản phẩm tặng

    @Override
    public BigDecimal calculateDiscount(Order order) {
        if (!isApplicable(order)) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalDiscount = BigDecimal.ZERO;

        for (OrderItem item : order.getOrderItems()) {
            int quantity = item.getQuantity();

            // Tính số lượng được tặng
            int freeQuantity = (quantity / (x + y)) * y;

            if (freeQuantity > 0) {
                BigDecimal itemPrice = item.getPrice();
                BigDecimal itemDiscount = itemPrice
                        .multiply(BigDecimal.valueOf(freeQuantity))
                        .multiply(discountPercentage)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                totalDiscount = totalDiscount.add(itemDiscount);
            }
        }

        return totalDiscount;
    }

    @Override
    public String getDescription() {
        return String.format("Mua %s tặng %s (giảm %s%% trên sản phẩm tặng)",
                x, y, discountPercentage);
    }
}