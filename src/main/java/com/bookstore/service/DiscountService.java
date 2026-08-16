package com.bookstore.service;

import com.bookstore.strategy.DiscountContext;
import com.bookstore.strategy.discount.BuyXGetYDiscount;
import com.bookstore.strategy.discount.FixedAmountDiscount;
import com.bookstore.strategy.discount.MemberDiscount;
import com.bookstore.strategy.discount.PercentageDiscount;
import com.bookstore.model.Order;
import com.bookstore.model.UserLoyalty;
import com.bookstore.repository.UserLoyaltyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscountService {
    private final DiscountContext discountContext;
    private final UserLoyaltyRepository userLoyaltyRepository;

    /**
     * Khởi tạo các discount strategies
     */
    public void initDiscounts() {
        // Percentage discount: 10%
        discountContext.addStrategy(new PercentageDiscount(BigDecimal.valueOf(10)));

        // Fixed amount: $5
        discountContext.addStrategy(new FixedAmountDiscount(BigDecimal.valueOf(5)));

        // Buy 2 Get 1 Free (50% off on free item)
        discountContext.addStrategy(new BuyXGetYDiscount(2, 1, BigDecimal.valueOf(50)));

        // Member discount sẽ được tính dựa trên tier của user
    }

    /**
     * Tính discount cho order
     */
    public BigDecimal calculateDiscount(Order order) {
        if (order == null || order.getUser() == null) {
            return BigDecimal.ZERO;
        }

        // Lấy tier của user
        String tier = userLoyaltyRepository.findByUserId(order.getUser().getId())
                .map(UserLoyalty::getTier)
                .orElse("BRONZE");

        // Thêm member discount dựa trên tier
        MemberDiscount memberDiscount = new MemberDiscount(tier);
        discountContext.addStrategy(memberDiscount);

        // Tính discount tốt nhất
        return discountContext.applyBestDiscount(order);
    }

    /**
     * Áp dụng discount vào order
     */
    public Order applyDiscountToOrder(Order order) {
        BigDecimal discount = calculateDiscount(order);

        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal finalTotal = order.getTotalAmount().subtract(discount);
            order.setTotalAmount(finalTotal.max(BigDecimal.ZERO));
            log.info("✅ Applied discount: ${}, final total: ${}", discount, order.getTotalAmount());
        }

        return order;
    }
}
