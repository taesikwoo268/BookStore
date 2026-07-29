-- ============================================================
-- V9__add_idempotency_table.sql
-- Tạo bảng lưu idempotency records để tránh double order
-- ============================================================

CREATE TABLE IF NOT EXISTS idempotency_records (
                                                   id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                   idempotency_key VARCHAR(100) NOT NULL COMMENT 'Idempotency-Key từ header',
                                                   user_id BIGINT NOT NULL COMMENT 'User ID',
                                                   resource_type VARCHAR(50) NOT NULL COMMENT 'ORDER, PAYMENT, REFUND',
                                                   resource_id BIGINT COMMENT 'ID của resource (order_id, payment_id)',
                                                   request_hash TEXT COMMENT 'Hash của request body',
                                                   response_status VARCHAR(10) COMMENT 'HTTP status code',
                                                   response_body TEXT COMMENT 'Response body đã serialized',
                                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                   expires_at TIMESTAMP COMMENT 'Thời gian hết hạn',
                                                   UNIQUE KEY uk_idempotency_key_user_id (idempotency_key, user_id),
                                                   INDEX idx_idempotency_user_id (user_id),
                                                   INDEX idx_idempotency_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;