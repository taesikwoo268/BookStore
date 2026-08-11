package com.bookstore.event;

import com.bookstore.observer.OrderObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@Slf4j
public class OrderSubject {

    private final List<OrderObserver> observers = new ArrayList<>();

    /**
     * Đăng ký observer
     */
    public void attach(OrderObserver observer) {
        observers.add(observer);
        observers.sort(Comparator.comparingInt(OrderObserver::getPriority));
        log.info("✅ Attached observer: {}", observer.getObserverName());
    }

    /**
     * Hủy đăng ký observer
     */
    public void detach(OrderObserver observer) {
        observers.remove(observer);
        log.info("❌ Detached observer: {}", observer.getObserverName());
    }

    /**
     * Thông báo cho tất cả observers
     */
    public void notifyObservers(OrderPlacedEvent event) {
        log.info("📢 Notifying {} observers about order {}", observers.size(), event.getOrderId());

        for (OrderObserver observer : observers) {
            try {
                observer.onOrderPlaced(event);
            } catch (Exception e) {
                log.error("❌ Observer {} failed for order {}: {}",
                        observer.getObserverName(), event.getOrderId(), e.getMessage());
                // Không throw exception để không ảnh hưởng các observer khác
            }
        }

        log.info("✅ All observers notified for order {}", event.getOrderId());
    }

    /**
     * Lấy danh sách observers hiện tại
     */
    public List<OrderObserver> getObservers() {
        return observers;
    }
}