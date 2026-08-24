-- =====================================================
-- V12__add_oauth2_fields.sql
-- Thêm cột cho OAuth2 login
-- =====================================================

-- Thêm cột provider và provider_id
ALTER TABLE users
    ADD COLUMN provider VARCHAR(20) NULL COMMENT 'Nhà cung cấp: google, facebook, github',
ADD COLUMN provider_id VARCHAR(100) NULL COMMENT 'ID từ nhà cung cấp OAuth2',
ADD COLUMN avatar_url VARCHAR(255) NULL COMMENT 'URL ảnh đại diện',
ADD COLUMN email_verified BOOLEAN DEFAULT FALSE COMMENT 'Email đã được xác thực';

-- Tạo index cho provider_id
CREATE INDEX idx_users_provider_id ON users(provider_id);