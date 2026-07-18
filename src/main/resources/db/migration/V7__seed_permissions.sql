-- =====================================================
-- V7__seed_permissions.sql
-- Seed dữ liệu cho permissions và role_permissions
-- =====================================================

-- ===== 1. PERMISSIONS =====
INSERT INTO permissions (name, resource, action, description) VALUES
-- Book permissions
('CREATE_BOOK', 'BOOK', 'CREATE', 'Tạo sách mới'),
('READ_BOOK', 'BOOK', 'READ', 'Xem thông tin sách'),
('UPDATE_BOOK', 'BOOK', 'UPDATE', 'Cập nhật sách'),
('DELETE_BOOK', 'BOOK', 'DELETE', 'Xóa sách'),

-- Author permissions
('CREATE_AUTHOR', 'AUTHOR', 'CREATE', 'Tạo tác giả mới'),
('READ_AUTHOR', 'AUTHOR', 'READ', 'Xem thông tin tác giả'),
('UPDATE_AUTHOR', 'AUTHOR', 'UPDATE', 'Cập nhật tác giả'),
('DELETE_AUTHOR', 'AUTHOR', 'DELETE', 'Xóa tác giả'),

-- Category permissions
('CREATE_CATEGORY', 'CATEGORY', 'CREATE', 'Tạo thể loại mới'),
('READ_CATEGORY', 'CATEGORY', 'READ', 'Xem thông tin thể loại'),
('UPDATE_CATEGORY', 'CATEGORY', 'UPDATE', 'Cập nhật thể loại'),
('DELETE_CATEGORY', 'CATEGORY', 'DELETE', 'Xóa thể loại'),

-- Order permissions
('CREATE_ORDER', 'ORDER', 'CREATE', 'Tạo đơn hàng mới'),
('READ_ORDER', 'ORDER', 'READ', 'Xem thông tin đơn hàng'),
('UPDATE_ORDER', 'ORDER', 'UPDATE', 'Cập nhật đơn hàng'),
('DELETE_ORDER', 'ORDER', 'DELETE', 'Xóa đơn hàng'),
('CANCEL_ORDER', 'ORDER', 'CANCEL', 'Hủy đơn hàng'),

-- User permissions
('READ_USER', 'USER', 'READ', 'Xem thông tin người dùng'),
('UPDATE_USER', 'USER', 'UPDATE', 'Cập nhật người dùng'),
('DELETE_USER', 'USER', 'DELETE', 'Xóa người dùng'),
('ASSIGN_ROLE', 'USER', 'ASSIGN_ROLE', 'Gán vai trò cho người dùng'),

-- Cart permissions
('READ_CART', 'CART', 'READ', 'Xem giỏ hàng'),
('UPDATE_CART', 'CART', 'UPDATE', 'Cập nhật giỏ hàng'),

-- Report permissions
('VIEW_REPORT', 'REPORT', 'VIEW', 'Xem báo cáo'),
('EXPORT_REPORT', 'REPORT', 'EXPORT', 'Xuất báo cáo'),

-- Admin permissions
('MANAGE_USERS', 'ADMIN', 'MANAGE_USERS', 'Quản lý người dùng'),
('MANAGE_ROLES', 'ADMIN', 'MANAGE_ROLES', 'Quản lý vai trò'),
('MANAGE_PERMISSIONS', 'ADMIN', 'MANAGE_PERMISSIONS', 'Quản lý quyền'),
('ACCESS_ADMIN_PANEL', 'ADMIN', 'ACCESS', 'Truy cập trang quản trị');

-- ===== 2. UPDATE ROLES =====
UPDATE roles SET
                 description = CASE name
                                   WHEN 'ROLE_ADMIN' THEN 'Quản trị viên hệ thống - Có toàn quyền'
                                   WHEN 'ROLE_MANAGER' THEN 'Quản lý - Có quyền quản lý nội dung'
                                   WHEN 'ROLE_EMPLOYEE' THEN 'Nhân viên - Có quyền cơ bản'
                                   WHEN 'ROLE_USER' THEN 'Người dùng thông thường'
                     END,
                 updated_at = NOW()
WHERE name IN ('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_EMPLOYEE', 'ROLE_USER');

-- ===== 3. ROLE_PERMISSIONS =====
-- ADMIN: Tất cả quyền
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN';

-- MANAGER: Quản lý nội dung (không có quyền admin)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_MANAGER'
  AND p.name NOT LIKE 'MANAGE_%'
  AND p.name NOT LIKE 'ACCESS_ADMIN_PANEL'
  AND p.name NOT LIKE 'DELETE_USER';

-- EMPLOYEE: Quyền cơ bản (xem và cập nhật đơn hàng)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_EMPLOYEE'
  AND p.name IN (
                 'READ_BOOK', 'READ_AUTHOR', 'READ_CATEGORY',
                 'READ_ORDER', 'UPDATE_ORDER', 'CANCEL_ORDER',
                 'READ_USER', 'READ_CART'
    );

-- USER: Quyền người dùng thông thường
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_USER'
  AND p.name IN (
                 'READ_BOOK', 'READ_AUTHOR', 'READ_CATEGORY',
                 'CREATE_ORDER', 'READ_ORDER', 'CANCEL_ORDER',
                 'READ_CART', 'UPDATE_CART'
    );

-- ===== 4. GÁN QUYỀN CHO USER ADMIN =====
-- Admin user (id=1) đã có ROLE_ADMIN nên có tất cả quyền

-- ===== 5. KIỂM TRA DỮ LIỆU =====
-- SELECT
--     r.name AS role_name,
--     GROUP_CONCAT(p.name) AS permissions
-- FROM roles r
-- JOIN role_permissions rp ON r.id = rp.role_id
-- JOIN permissions p ON p.id = rp.permission_id
-- GROUP BY r.name
-- ORDER BY r.name;

-- ===== 6. THÊM CÁC USER MỚI =====
INSERT INTO users (username, password, email, full_name, enabled, created_at) VALUES
                                                                                  ('manager', '$2a$10$NkM5qS2Z5q2pJ.8hZ4Q.0uD6KJ9XbZ7XbZ7XbZ7XbZ7XbZ7XbZ7XbZ7', 'manager@bookstore.com', 'Manager User', TRUE, NOW()),
                                                                                  ('employee', '$2a$10$NkM5qS2Z5q2pJ.8hZ4Q.0uD6KJ9XbZ7XbZ7XbZ7XbZ7XbZ7XbZ7XbZ7', 'employee@bookstore.com', 'Employee User', TRUE, NOW()),
                                                                                  ('user1', '$2a$10$NkM5qS2Z5q2pJ.8hZ4Q.0uD6KJ9XbZ7XbZ7XbZ7XbZ7XbZ7XbZ7XbZ7', 'user1@bookstore.com', 'User One', TRUE, NOW()),
                                                                                  ('user2', '$2a$10$NkM5qS2Z5q2pJ.8hZ4Q.0uD6KJ9XbZ7XbZ7XbZ7XbZ7XbZ7XbZ7XbZ7', 'user2@bookstore.com', 'User Two', TRUE, NOW());

-- ===== 7. GÁN VAI TRÒ CHO USER MỚI =====
INSERT INTO user_roles (user_id, role_id) VALUES
                                              ((SELECT id FROM users WHERE username = 'manager'), (SELECT id FROM roles WHERE name = 'ROLE_MANAGER')),
                                              ((SELECT id FROM users WHERE username = 'employee'), (SELECT id FROM roles WHERE name = 'ROLE_EMPLOYEE')),
                                              ((SELECT id FROM users WHERE username = 'user1'), (SELECT id FROM roles WHERE name = 'ROLE_USER')),
                                              ((SELECT id FROM users WHERE username = 'user2'), (SELECT id FROM roles WHERE name = 'ROLE_USER'));