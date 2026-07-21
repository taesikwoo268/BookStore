-- =====================================================
-- V8__add_refresh_tokens.sql
-- Tạo bảng refresh_tokens cho JWT Refresh Token
-- =====================================================

-- Tạo bảng refresh_tokens
CREATE TABLE IF NOT EXISTS refresh_tokens (
                                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                              token_id VARCHAR(36) NOT NULL UNIQUE COMMENT 'UUID token để tìm kiếm',
                                              token_hash VARCHAR(255) NOT NULL COMMENT 'BCrypt hash của token',
                                              user_id BIGINT NOT NULL COMMENT 'ID của user sở hữu token',
                                              expiry_date TIMESTAMP NOT NULL COMMENT 'Thời gian hết hạn',
                                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời gian tạo',
                                              revoked_at TIMESTAMP NULL COMMENT 'Thời gian bị thu hồi',
                                              revoked BOOLEAN DEFAULT FALSE COMMENT 'Đã bị thu hồi chưa',
                                              revoked_reason VARCHAR(100) COMMENT 'Lý do thu hồi (LOGOUT, ROTATED, EXPIRED)',
                                              parent_token_id BIGINT COMMENT 'ID của token cha (theo dõi rotation chain)',
                                              ip_address VARCHAR(45) COMMENT 'IP address khi tạo token',
                                              user_agent VARCHAR(255) COMMENT 'User Agent khi tạo token',

                                              FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

                                              INDEX idx_refresh_tokens_token_id (token_id),
                                              INDEX idx_refresh_tokens_user_id (user_id),
                                              INDEX idx_refresh_tokens_user_revoked (user_id, revoked),
                                              INDEX idx_refresh_tokens_expiry (expiry_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;