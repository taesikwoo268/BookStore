-- =====================================================
-- V8__add_refresh_tokens.sql
-- Tạo bảng refresh_tokens cho JWT Refresh Token
-- =====================================================

-- Tạo bảng refresh_tokens
CREATE TABLE IF NOT EXISTS refresh_tokens (
                                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                              token VARCHAR(255) NOT NULL UNIQUE,
                                              user_id BIGINT NOT NULL,
                                              expiry_date TIMESTAMP NOT NULL,
                                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                              revoked BOOLEAN DEFAULT FALSE,
                                              FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                              INDEX idx_refresh_tokens_token (token),
                                              INDEX idx_refresh_tokens_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;