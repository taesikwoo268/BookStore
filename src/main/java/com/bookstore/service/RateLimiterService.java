package com.bookstore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimiterService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:search:";
    private static final int MAX_REQUESTS = 30;
    private static final int WINDOW_SECONDS = 60;

    /**
     * Kiểm tra rate limit với sliding window
     * @param clientIp IP của client
     * @return true nếu được phép, false nếu bị chặn
     */
    public boolean isAllowed(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) {
            clientIp = "unknown";
        }

        String key = RATE_LIMIT_KEY_PREFIX + clientIp;
        long now = Instant.now().getEpochSecond();

        // 1. Xóa các request cũ hơn WINDOW_SECONDS
        Long removed = redisTemplate.opsForZSet().removeRangeByScore(key, 0, now - WINDOW_SECONDS);
        if (removed != null && removed > 0) {
            log.debug("🗑️ Removed {} expired requests for IP: {}", removed, clientIp);
        }

        // 2. Đếm số request trong window hiện tại
        Long currentCount = redisTemplate.opsForZSet().zCard(key);
        if (currentCount == null) {
            currentCount = 0L;
        }

        log.debug("📊 IP: {} - Requests: {}/{} in {}s window", clientIp, currentCount, MAX_REQUESTS, WINDOW_SECONDS);

        // 3. Kiểm tra nếu vượt quá giới hạn
        if (currentCount >= MAX_REQUESTS) {
            log.warn("🚫 Rate limit exceeded for IP: {} - {} requests", clientIp, currentCount);
            return false;
        }

        // 4. Thêm request mới vào window
        redisTemplate.opsForZSet().add(key, String.valueOf(now), now);

        // 5. Set TTL cho key (dọn dẹp tự động)
        redisTemplate.expire(key, java.time.Duration.ofSeconds(WINDOW_SECONDS + 10));

        log.debug("✅ Allowed request for IP: {} ({}/{})", clientIp, currentCount + 1, MAX_REQUESTS);
        return true;
    }

    /**
     * Lấy số request còn lại của IP
     */
    public long getRemainingRequests(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) {
            clientIp = "unknown";
        }

        String key = RATE_LIMIT_KEY_PREFIX + clientIp;
        long now = Instant.now().getEpochSecond();

        // Xóa request cũ
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, now - WINDOW_SECONDS);

        Long currentCount = redisTemplate.opsForZSet().zCard(key);
        long count = currentCount != null ? currentCount : 0;

        return Math.max(0, MAX_REQUESTS - count);
    }

    /**
     * Reset rate limit cho IP
     */
    public void resetRateLimit(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) {
            clientIp = "unknown";
        }
        String key = RATE_LIMIT_KEY_PREFIX + clientIp;
        redisTemplate.delete(key);
        log.info("🔄 Reset rate limit for IP: {}", clientIp);
    }

    /**
     * Lấy thời gian reset (giây)
     */
    public long getResetTime(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) {
            clientIp = "unknown";
        }

        String key = RATE_LIMIT_KEY_PREFIX + clientIp;
        Long ttl = redisTemplate.getExpire(key);
        return ttl != null ? ttl : 0;
    }
}