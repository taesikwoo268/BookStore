package com.bookstore.service;

import com.bookstore.exception.InvalidTokenException;
import com.bookstore.model.RefreshToken;
import com.bookstore.model.User;
import com.bookstore.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    private final long REFRESH_TOKEN_EXPIRY_MS = 7 * 24 * 60 * 60 * 1000; // 7 days

    /**
     * 1️⃣ Tạo Refresh Token mới (Hash + Lưu DB)
     */
    @Transactional
    public String createRefreshToken(User user, String ipAddress, String userAgent) {
        String plainToken = UUID.randomUUID().toString();
        String hashedToken = passwordEncoder.encode(plainToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .tokenId(plainToken) // Store the plain token for lookup
                .tokenHash(hashedToken)
                .userId(user.getId())
                .expiryDate(Instant.now().plusMillis(REFRESH_TOKEN_EXPIRY_MS))
                .createdAt(Instant.now())
                .revoked(false)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        refreshTokenRepository.save(refreshToken);
        log.info("Created refresh token for user: {}", user.getUsername());
        return plainToken; // Return the plain token to the client
    }

    /**
     * 2️⃣ Xác thực và lấy Refresh Token (So sánh hash)
     */
    @Transactional
    public RefreshToken validateAndGetRefreshToken(String plainToken) {
//        1. Tìm tất cả token của user (không thể tìm theo hash vì BCrypt)
        RefreshToken refreshToken = refreshTokenRepository.findByTokenId(plainToken)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        // 2. Kiểm tra active
        if (!refreshToken.isActive()) {
            if (refreshToken.isExpired()) {
                refreshToken.setRevoked(true);
                refreshToken.setRevokedReason("EXPIRED");
                refreshTokenRepository.save(refreshToken);
                throw new InvalidTokenException("Refresh token has expired");
            }
            if (refreshToken.getRevoked()) {
                throw new InvalidTokenException("Refresh token has been revoked");
            }
        }

        // 3. So sánh hash (nếu dùng hash)
         if (!passwordEncoder.matches(plainToken, refreshToken.getTokenHash())) {
             throw new InvalidTokenException("Invalid refresh token");
         }

        return refreshToken;
    }
    /**
     * 3️⃣ ROTATE: Tạo token mới, vô hiệu hóa token cũ
     */
    @Transactional
    public String rotateRefreshToken(String oldPlainToken, User user, String ipAddress, String userAgent) {
        // 1. Lấy và validate token cũ
        RefreshToken oldToken = validateAndGetRefreshToken(oldPlainToken);

        // 2. Đánh dấu token cũ revoked (ROTATED)
        oldToken.setRevoked(true);
        oldToken.setRevokedAt(Instant.now());
        oldToken.setRevokedReason("ROTATED");
        refreshTokenRepository.save(oldToken);

        // 3. Tạo token mới (liên kết với token cũ)
        String newPlainToken = UUID.randomUUID().toString();
        String hashedToken = passwordEncoder.encode(newPlainToken);

        RefreshToken newToken = RefreshToken.builder()
                .tokenId(newPlainToken)
                .tokenHash(hashedToken)
                .userId(user.getId())
                .expiryDate(Instant.now().plusMillis(REFRESH_TOKEN_EXPIRY_MS))
                .createdAt(Instant.now())
                .revoked(false)
                .parentTokenId(oldToken.getId())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        refreshTokenRepository.save(newToken);
        log.info("🔄 Refresh token rotated for user: {}, old: {}, new: {}",
                user.getUsername(), oldToken.getId(), newToken.getId());

        return newPlainToken;
    }
    /**
     * 4️⃣ BLACKLIST: Thu hồi token khi logout
     */
    @Transactional
    public void blacklistRefreshToken(String plainToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenId(plainToken)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        refreshToken.setRevoked(true);
        refreshToken.setRevokedAt(Instant.now());
        refreshToken.setRevokedReason("LOGOUT");
        refreshTokenRepository.save(refreshToken);

        log.info("⬛ Refresh token blacklisted: {}", refreshToken.getId());
    }

    /**
     * 5️⃣ BLACKLIST ALL: Thu hồi tất cả token của user
     */
    @Transactional
    public void blacklistAllUserTokens(Long userId, String reason) {
        refreshTokenRepository.findByUserId(userId).forEach(token -> {
            token.setRevoked(true);
            token.setRevokedAt(Instant.now());
            token.setRevokedReason(reason);
        });
        refreshTokenRepository.saveAll(refreshTokenRepository.findByUserId(userId));
        log.info("⬛ All tokens blacklisted for user: {}", userId);
    }

    /**
     * 6️⃣ Xóa token đã expired (Scheduled)
     */
    @Transactional
    public void deleteExpiredTokens() {
        Instant now = Instant.now();
        int deleted = refreshTokenRepository.deleteByExpiryDateBefore(now);
        log.info("🧹 Deleted {} expired refresh tokens", deleted);
    }
}
