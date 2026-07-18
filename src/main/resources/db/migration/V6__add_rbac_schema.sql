-- =====================================================
-- V6__add_rbac_schema.sql
-- Thêm bảng permissions và role_permissions cho RBAC
-- =====================================================

-- ===== 1. TẠO BẢNG PERMISSIONS =====
CREATE TABLE IF NOT EXISTS permissions (
                                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           name VARCHAR(100) NOT NULL UNIQUE COMMENT 'Tên quyền (VD: READ_BOOK, WRITE_BOOK)',
    resource VARCHAR(50) NOT NULL COMMENT 'Tài nguyên (VD: BOOK, AUTHOR, CATEGORY, ORDER)',
    action VARCHAR(50) NOT NULL COMMENT 'Hành động (VD: CREATE, READ, UPDATE, DELETE)',
    description VARCHAR(255) COMMENT 'Mô tả quyền',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_permissions_resource (resource),
    INDEX idx_permissions_action (action),
    UNIQUE KEY uk_permissions_name (name)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===== 2. TẠO BẢNG ROLE_PERMISSIONS (Quan hệ nhiều-nhiều) =====
CREATE TABLE IF NOT EXISTS role_permissions (
                                                role_id BIGINT NOT NULL,
                                                permission_id BIGINT NOT NULL,
                                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                                PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,

    INDEX idx_role_permissions_role_id (role_id),
    INDEX idx_role_permissions_permission_id (permission_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===== 3. THÊM CỘT MỚI VÀO BẢNG USERS =====
ALTER TABLE users
    ADD COLUMN enabled BOOLEAN DEFAULT TRUE COMMENT 'Tài khoản có hoạt động không',
ADD COLUMN account_non_expired BOOLEAN DEFAULT TRUE COMMENT 'Tài khoản không hết hạn',
ADD COLUMN account_non_locked BOOLEAN DEFAULT TRUE COMMENT 'Tài khoản không bị khóa',
ADD COLUMN credentials_non_expired BOOLEAN DEFAULT TRUE COMMENT 'Mật khẩu không hết hạn',
ADD COLUMN last_login TIMESTAMP NULL COMMENT 'Lần đăng nhập cuối',
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- ===== 4. UPDATE ROLES (Thêm description) =====
ALTER TABLE roles
    ADD COLUMN description VARCHAR(255) COMMENT 'Mô tả vai trò',
ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- ===== 5. INDEXES CHO HIỆU SUẤT =====
CREATE INDEX idx_users_enabled ON users(enabled);
CREATE INDEX idx_users_last_login ON users(last_login);
CREATE INDEX idx_roles_name ON roles(name);