package com.bookstore.config;

import com.bookstore.event.OrderSubject;
import com.bookstore.observer.AnalyticsObserver;
import com.bookstore.observer.EmailObserver;
import com.bookstore.observer.LoyaltyObserver;
import com.bookstore.observer.OrderObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ObserverConfig {

    private final OrderSubject orderSubject;
    private final LoyaltyObserver loyaltyObserver;
    private final EmailObserver emailObserver;
    private final AnalyticsObserver analyticsObserver;

    @PostConstruct
    public void initObservers() {
        log.info("🔧 Initializing observers...");

        // Đăng ký observers theo thứ tự ưu tiên
        orderSubject.attach(loyaltyObserver);   // Priority 0
        orderSubject.attach(emailObserver);     // Priority 1
        orderSubject.attach(analyticsObserver); // Priority 2

        log.info("✅ {} observers registered", orderSubject.getObservers().size());
    }
}