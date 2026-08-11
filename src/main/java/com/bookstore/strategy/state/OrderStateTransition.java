package com.bookstore.strategy.state;

import com.bookstore.enums.OrderStatus;
import com.bookstore.model.Order;

/**
 * Strategy interface cho transition trạng thái đơn hàng
 */
public interface OrderStateTransition {

    /**
     * Kiểm tra transition có hợp lệ không
     */
    boolean canTransition(OrderStatus currentStatus, OrderStatus targetStatus);

    /**
     * Thực hiện transition
     */
    Order execute(Order order, OrderStatus targetStatus, String reason);

    /**
     * Mô tả transition
     */
    default String getDescription() {
        return this.getClass().getSimpleName();
    }
}