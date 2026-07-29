package com.bookstore.service;

import com.bookstore.model.IdempotencyRecord;
import com.bookstore.repository.IdempotencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {

    private final IdempotencyRepository idempotencyRepository;

    /**
     * Kiểm tra xem request đã được xử lý chưa
     * @return Optional chứa record nếu đã tồn tại
     */
    public Optional<IdempotencyRecord> getExistingRecord(String key, Long userId) {
        return idempotencyRepository.findByIdempotencyKeyAndUserId(key, userId);
    }

    /**
     * Lưu record sau khi xử lý thành công
     */
    @Transactional
    public void saveRecord(String key, Long userId, String resourceType,
                           Long resourceId, String requestHash,
                           String responseStatus, String responseBody,
                           int ttlHours) {
        IdempotencyRecord record = IdempotencyRecord.builder()
                .idempotencyKey(key)
                .userId(userId)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .requestHash(requestHash)
                .responseStatus(responseStatus)
                .responseBody(responseBody)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(ttlHours))
                .build();

        idempotencyRepository.save(record);
        log.info("✅ Idempotency record saved for key: {}", key);
    }

    /**
     * Xóa các record đã hết hạn (chạy định kỳ)
     */
    @Transactional
    public int deleteExpiredRecords() {
        int deleted = idempotencyRepository.deleteAllExpired(LocalDateTime.now());
        if (deleted > 0) {
            log.info("🧹 Deleted {} expired idempotency records", deleted);
        }
        return deleted;
    }

    /**
     * Tạo hash từ request body
     */
    public String generateRequestHash(Object body) {
        if (body == null) {
            return "";
        }
        // Dùng SHA-256 để tạo hash
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(body.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.warn("Failed to hash request body: {}", e.getMessage());
            return "";
        }
    }
}