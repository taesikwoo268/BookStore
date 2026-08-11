package com.bookstore.observer;

import com.bookstore.event.OrderPlacedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class AnalyticsObserver implements OrderObserver {

    private final Map<Long, Integer> conversionCounts = new HashMap<>();
    private double totalRevenue = 0;
    private int totalOrders = 0;

    @Override
    public void onOrderPlaced(OrderPlacedEvent event) {
        log.info("📊 [AnalyticsObserver] Processing analytics for order {}", event.getOrderId());

        try {
            // ===== 1. TRACK CONVERSION =====
            trackConversion(event.getUserId());

            // ===== 2. UPDATE REVENUE =====
            updateRevenue(event.getTotalAmount().doubleValue());

            // ===== 3. TRACK USER BEHAVIOR =====
            trackUserBehavior(event);

            // ===== 4. LOG METRICS =====
            logAnalytics();

            log.info("✅ [AnalyticsObserver] Analytics processed for order {}", event.getOrderId());

        } catch (Exception e) {
            log.error("❌ [AnalyticsObserver] Failed to process analytics for order {}: {}",
                    event.getOrderId(), e.getMessage());
        }
    }

    @Override
    public int getPriority() {
        return 2; // Chạy sau EmailObserver
    }

    private void trackConversion(Long userId) {
        conversionCounts.put(userId, conversionCounts.getOrDefault(userId, 0) + 1);
        totalOrders++;
        log.info("📊 Conversion tracked for user: {} (total conversions: {})",
                userId, conversionCounts.get(userId));
    }

    private void updateRevenue(double amount) {
        totalRevenue += amount;
        log.info("📊 Revenue updated: +${}, total: ${}", amount, totalRevenue);
    }

    private void trackUserBehavior(OrderPlacedEvent event) {
        int itemCount = event.getItems().size();
        log.info("📊 User {} ordered {} items, total: ${}",
                event.getUserId(), itemCount, event.getTotalAmount());

        // Tần suất mua hàng
        int conversionCount = conversionCounts.getOrDefault(event.getUserId(), 0);
        log.info("📊 User {} has placed {} orders", event.getUserId(), conversionCount);
    }

    private void logAnalytics() {
        log.info("=========================================");
        log.info("📊 ANALYTICS REPORT");
        log.info("=========================================");
        log.info("📊 Total Orders: {}", totalOrders);
        log.info("📊 Total Revenue: ${}", totalRevenue);
        log.info("📊 Average Order Value: ${}",
                totalOrders > 0 ? totalRevenue / totalOrders : 0);
        log.info("📊 Unique Users: {}", conversionCounts.size());
        log.info("=========================================");
    }

    public Map<Long, Integer> getConversionCounts() {
        return conversionCounts;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public int getTotalOrders() {
        return totalOrders;
    }
}