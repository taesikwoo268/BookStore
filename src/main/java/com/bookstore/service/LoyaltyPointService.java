package com.bookstore.service;

import com.bookstore.model.LoyaltyPoint;
import com.bookstore.model.UserLoyalty;
import com.bookstore.repository.LoyaltyPointRepository;
import com.bookstore.repository.UserLoyaltyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoyaltyPointService {

    private final LoyaltyPointRepository loyaltyPointRepository;
    private final UserLoyaltyRepository userLoyaltyRepository;

    private static final int POINTS_PER_DOLLAR = 10; // 1 điểm = $10
    private static final int BRONZE_THRESHOLD = 0;
    private static final int SILVER_THRESHOLD = 1000;
    private static final int GOLD_THRESHOLD = 5000;
    private static final int PLATINUM_THRESHOLD = 10000;

    /**
     * Tính điểm từ số tiền
     */
    public int calculatePoints(BigDecimal totalAmount) {
        if (totalAmount == null) {
            return 0;
        }
        return totalAmount.intValue() * POINTS_PER_DOLLAR;
    }

    /**
     * Cộng điểm cho user
     */
    @Transactional
    public void addLoyaltyPoints(Long userId, Long orderId, BigDecimal totalAmount) {
        log.info("⭐ [LoyaltyPoint] Calculating points for user: {}, order: {}", userId, orderId);

        // 1. Tính điểm
        int points = calculatePoints(totalAmount);
        if (points <= 0) {
            log.warn("⭐ [LoyaltyPoint] No points earned for order: {}", orderId);
            return;
        }

        // 2. Lưu lịch sử điểm
        LoyaltyPoint loyaltyPoint = LoyaltyPoint.builder()
                .userId(userId)
                .orderId(orderId)
                .points(points)
                .description("Earned from order #" + orderId)
                .transactionType("EARNED")
                .build();
        loyaltyPointRepository.save(loyaltyPoint);

        // 3. Cập nhật tổng điểm
        updateUserTotalPoints(userId, points);
        log.info("⭐ [LoyaltyPoint] Added {} points to user: {}, order: {}", points, userId, orderId);
    }

    /**
     * Cập nhật tổng điểm của user
     */
    @Transactional
    public void updateUserTotalPoints(Long userId, int points) {
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

    /**
     * Tính tier dựa trên tổng điểm
     */
    public String calculateTier(int totalPoints) {
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

    /**
     * Lấy tổng điểm của user
     */
    public Integer getTotalPoints(Long userId) {
        return userLoyaltyRepository.findByUserId(userId)
                .map(UserLoyalty::getTotalPoints)
                .orElse(0);
    }

    /**
     * Lấy tier của user
     */
    public String getUserTier(Long userId) {
        return userLoyaltyRepository.findByUserId(userId)
                .map(UserLoyalty::getTier)
                .orElse("BRONZE");
    }
}