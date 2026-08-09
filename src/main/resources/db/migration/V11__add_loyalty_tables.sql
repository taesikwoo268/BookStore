-- ============================================================
-- V11__add_loyalty_tables.sql
-- Tạo bảng loyalty_points và user_loyalty
-- ============================================================

-- Bảng loyalty_points (lịch sử điểm)
CREATE TABLE IF NOT EXISTS loyalty_points (
                                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                              user_id BIGINT NOT NULL,
                                              order_id BIGINT,
                                              points INT NOT NULL,
                                              description VARCHAR(255),
    transaction_type VARCHAR(50) DEFAULT 'EARNED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_loyalty_points_user_id (user_id),
    INDEX idx_loyalty_points_order_id (order_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Bảng user_loyalty (tổng điểm)
CREATE TABLE IF NOT EXISTS user_loyalty (
                                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            user_id BIGINT NOT NULL UNIQUE,
                                            total_points INT DEFAULT 0,
                                            total_spent DECIMAL(10,2) DEFAULT 0.00,
    tier VARCHAR(20) DEFAULT 'BRONZE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_loyalty_user_id (user_id),
    INDEX idx_user_loyalty_tier (tier)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;