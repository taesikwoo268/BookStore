package com.bookstore.observer;

import com.bookstore.event.OrderPlacedEvent;
import com.bookstore.model.LoyaltyPoint;
import com.bookstore.model.UserLoyalty;
import com.bookstore.repository.LoyaltyPointRepository;
import com.bookstore.repository.UserLoyaltyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoyaltyObserver implements OrderObserver {

    private final LoyaltyPointRepository loyaltyPointRepository;
    private final UserLoyaltyRepository userLoyaltyRepository;

    private static final int POINTS_PER_DOLLAR = 10; // 1 điểm = $10
    private static final int SILVER_THRESHOLD = 1000;
    private static final int GOLD_THRESHOLD = 5000;
    private static final int PLATINUM_THRESHOLD = 10000;

    @Override
    @Transactional
    public void onOrderPlaced(OrderPlacedEvent event) {
        log.info("⭐ [LoyaltyObserver] Processing loyalty points for user {} - order {}",
                event.getUserId(), event.getOrderId());

        try {
            // ===== 1. CALCULATE POINTS =====
            int points = calculatePoints(event.getTotalAmount());
            log.info("⭐ Calculated {} points for order {}", points, event.getOrderId());

            if (points <= 0) {
                log.info("⭐ No points earned for order {}", event.getOrderId());
                return;
            }

            // ===== 2. SAVE LOYALTY POINT HISTORY =====
            LoyaltyPoint loyaltyPoint = LoyaltyPoint.builder()
                    .userId(event.getUserId())
                    .orderId(event.getOrderId())
                    .points(points)
                    .description("Earned from order #" + event.getOrderNumber())
                    .transactionType("EARNED")
                    .build();
            loyaltyPointRepository.save(loyaltyPoint);

            // ===== 3. UPDATE USER TOTAL POINTS =====
            updateUserTotalPoints(event.getUserId(), points);

            // ===== 4. LOG RESULT =====
            Integer totalPoints = getTotalPoints(event.getUserId());
            String tier = getUserTier(event.getUserId());
            log.info("⭐ User {} earned {} points for order {}", event.getUserId(), points, event.getOrderId());
            log.info("⭐ User {} total points: {}, tier: {}", event.getUserId(), totalPoints, tier);

        } catch (Exception e) {
            log.error("❌ [LoyaltyObserver] Failed to process loyalty for order {}: {}",
                    event.getOrderId(), e.getMessage());
        }
    }

    @Override
    public int getPriority() {
        return 0; // Chạy đầu tiên
    }

    private int calculatePoints(BigDecimal totalAmount) {
        if (totalAmount == null) {
            return 0;
        }
        return totalAmount.intValue() * POINTS_PER_DOLLAR;
    }

    private void updateUserTotalPoints(Long userId, int points) {
        Optional<UserLoyalty> optional = userLoyaltyRepository.findByUserId(userId);
        UserLoyalty userLoyalty;

        if (optional.isPresent()) {
            userLoyalty = optional.get();
            userLoyalty.setTotalPoints(userLoyalty.getTotalPoints() + points);
            userLoyalty.setTier(calculateTier(userLoyalty.getTotalPoints()));
        } else {
            userLoyalty = UserLoyalty.builder()
                    .userId(userId)
                    .totalPoints(points)
                    .tier(calculateTier(points))
                    .build();
        }

        userLoyaltyRepository.save(userLoyalty);
    }

    private String calculateTier(int totalPoints) {
        if (totalPoints >= PLATINUM_THRESHOLD) {
            return "PLATINUM";
        } else if (totalPoints >= GOLD_THRESHOLD) {
            return "GOLD";
        } else if (totalPoints >= SILVER_THRESHOLD) {
            return "SILVER";
        } else {
            return "BRONZE";
        }
    }

    private Integer getTotalPoints(Long userId) {
        return userLoyaltyRepository.findByUserId(userId)
                .map(UserLoyalty::getTotalPoints)
                .orElse(0);
    }

    private String getUserTier(Long userId) {
        return userLoyaltyRepository.findByUserId(userId)
                .map(UserLoyalty::getTier)
                .orElse("BRONZE");
    }
}