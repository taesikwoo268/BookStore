package com.bookstore.enums;

import java.util.EnumSet;
import java.util.Set;

public enum OrderStatus {
    PENDING("Pending"),
    PROCESSING("Processing"),
    CONFIRMED("Confirmed"),
    SHIPPED("Shipped"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled"),
    REFUNDED("Refunded");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isActive() {
        return this == PENDING || this == PROCESSING || this == SHIPPED || this == CONFIRMED;
    }

    public boolean isFinal() {
        return this == DELIVERED || this == CANCELLED || this == REFUNDED;
    }

    public boolean isCancellable() {
        return this == PENDING || this == PROCESSING || this == CONFIRMED;
    }

    /**
     * Kiểm tra trạng thái có thể chuyển sang trạng thái khác không
     */
    public boolean canTransitionTo(OrderStatus target) {
        return ALLOWED_TRANSITIONS.get(this).contains(target);
    }

    /**
     * Định nghĩa các transition hợp lệ
     */
    private static final Set<OrderStatus> PENDING_TRANSITIONS =
            EnumSet.of(CONFIRMED, PROCESSING, CANCELLED);

    private static final Set<OrderStatus> PROCESSING_TRANSITIONS =
            EnumSet.of(CONFIRMED, SHIPPED, CANCELLED);

    private static final Set<OrderStatus> CONFIRMED_TRANSITIONS =
            EnumSet.of(PROCESSING, SHIPPED, CANCELLED);

    private static final Set<OrderStatus> SHIPPED_TRANSITIONS =
            EnumSet.of(DELIVERED);

    private static final Set<OrderStatus> DELIVERED_TRANSITIONS =
            EnumSet.noneOf(OrderStatus.class);  // Final state

    private static final Set<OrderStatus> CANCELLED_TRANSITIONS =
            EnumSet.noneOf(OrderStatus.class);  // Final state

    private static final Set<OrderStatus> REFUNDED_TRANSITIONS =
            EnumSet.noneOf(OrderStatus.class);  // Final state

    private static final java.util.Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS =
            new java.util.EnumMap<>(OrderStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(PENDING, PENDING_TRANSITIONS);
        ALLOWED_TRANSITIONS.put(PROCESSING, PROCESSING_TRANSITIONS);
        ALLOWED_TRANSITIONS.put(CONFIRMED, CONFIRMED_TRANSITIONS);
        ALLOWED_TRANSITIONS.put(SHIPPED, SHIPPED_TRANSITIONS);
        ALLOWED_TRANSITIONS.put(DELIVERED, DELIVERED_TRANSITIONS);
        ALLOWED_TRANSITIONS.put(CANCELLED, CANCELLED_TRANSITIONS);
        ALLOWED_TRANSITIONS.put(REFUNDED, REFUNDED_TRANSITIONS);
    }
}