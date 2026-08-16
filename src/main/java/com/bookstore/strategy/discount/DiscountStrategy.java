package com.bookstore.strategy.discount;

import com.bookstore.model.Order;

import java.math.BigDecimal;

public interface DiscountStrategy {

    /**
     * Tính số tiền giảm giá cho đơn hàng
     * @param order Đơn hàng cần tính
     * @return Số tiền giảm giá
     */
    BigDecimal calculateDiscount(Order order);

    /**
     * Mô tả chi tiết về discount
     */
    String getDescription();

    /**
     * Kiểm tra discount có áp dụng được cho order không
     */
    default boolean isApplicable(Order order) {
        return order != null && order.getOrderItems() != null && !order.getOrderItems().isEmpty();
    }
}