package com.bookstore.observer;

import com.bookstore.event.OrderPlacedEvent;

/**
 * Observer interface cho các sự kiện đơn hàng
 */
public interface OrderObserver {

    /**
     * Xử lý khi order được đặt thành công
     */
    void onOrderPlaced(OrderPlacedEvent event);

    /**
     * Tên observer
     */
    default String getObserverName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Độ ưu tiên (số càng nhỏ càng chạy trước)
     */
    default int getPriority() {
        return 0;
    }
}