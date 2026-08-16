package com.bookstore.strategy;

import com.bookstore.strategy.discount.DiscountStrategy;
import com.bookstore.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DiscountContext {

    private final List<DiscountStrategy> strategies = new ArrayList<>();

    /**
     * Thêm strategy vào context
     */
    public void addStrategy(DiscountStrategy strategy) {
        strategies.add(strategy);
        log.info("✅ Added discount strategy: {}", strategy.getDescription());
    }

    /**
     * Thêm nhiều strategies
     */
    public void addStrategies(List<DiscountStrategy> strategies) {
        this.strategies.addAll(strategies);
        log.info("✅ Added {} discount strategies", strategies.size());
    }

    /**
     * Áp dụng discount tốt nhất (lớn nhất)
     */
    public BigDecimal applyBestDiscount(Order order) {
        if (strategies.isEmpty()) {
            log.info("No discount strategies available");
            return BigDecimal.ZERO;
        }

        BigDecimal maxDiscount = BigDecimal.ZERO;
        DiscountStrategy bestStrategy = null;

        for (DiscountStrategy strategy : strategies) {
            if (strategy.isApplicable(order)) {
                BigDecimal discount = strategy.calculateDiscount(order);
                log.info("💰 {}: ${}", strategy.getDescription(), discount);

                if (discount.compareTo(maxDiscount) > 0) {
                    maxDiscount = discount;
                    bestStrategy = strategy;
                }
            }
        }

        if (bestStrategy != null) {
            log.info("✅ Best discount: {} - ${}",
                    bestStrategy.getDescription(), maxDiscount);
        }

        return maxDiscount;
    }

    /**
     * Áp dụng tất cả các discount (cộng dồn)
     */
    public BigDecimal applyAllDiscounts(Order order) {
        BigDecimal totalDiscount = BigDecimal.ZERO;

        for (DiscountStrategy strategy : strategies) {
            if (strategy.isApplicable(order)) {
                BigDecimal discount = strategy.calculateDiscount(order);
                log.info("💰 {}: ${}", strategy.getDescription(), discount);
                totalDiscount = totalDiscount.add(discount);
            }
        }

        log.info("💰 Total discount: ${}", totalDiscount);
        return totalDiscount;
    }

    /**
     * Lấy danh sách strategies hiện có
     */
    public List<DiscountStrategy> getStrategies() {
        return strategies;
    }
}