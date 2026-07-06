-- =====================================================
-- V3__seed_data.sql
-- Dữ liệu mẫu tối giản cho BookStore
-- =====================================================

-- 1. Roles
INSERT INTO roles (name) VALUES
                             ('ROLE_ADMIN'),
                             ('ROLE_USER');

-- 2. Users (mật khẩu BCrypt: '123456')
INSERT INTO users (username, password, email, full_name, created_at) VALUES
                                                                         ('admin', '$2a$10$NkM5qS2Z5q2pJ.8hZ4Q.0uD6KJ9XbZ7XbZ7XbZ7XbZ7XbZ7XbZ7XbZ7', 'admin@bookstore.com', 'Administrator', NOW()),
                                                                         ('john_doe', '$2a$10$NkM5qS2Z5q2pJ.8hZ4Q.0uD6KJ9XbZ7XbZ7XbZ7XbZ7XbZ7XbZ7XbZ7', 'john.doe@email.com', 'John Doe', NOW());

-- 3. User roles
INSERT INTO user_roles (user_id, role_id) VALUES
                                              (1, 1), -- admin có ROLE_ADMIN
                                              (1, 2), -- admin cũng có ROLE_USER
                                              (2, 2); -- john_doe có ROLE_USER

-- 4. Authors
INSERT INTO authors (name, biography) VALUES
                                          ('J.K. Rowling', 'British author, best known for Harry Potter series.'),
                                          ('George R.R. Martin', 'American author of A Song of Ice and Fire.');

-- 5. Categories
INSERT INTO categories (name, description) VALUES
                                               ('Fantasy', 'Books about magic and imaginary worlds.'),
                                               ('Science Fiction', 'Books about future technology and space.');

-- 6. Books
INSERT INTO books (isbn, title, price, stock, sales_count, author_id, category_id) VALUES
                                                                                       ('978-0439708184', 'Harry Potter and the Sorcerer''s Stone', 19.99, 100, 150, 1, 1),
                                                                                       ('978-0553103540', 'A Game of Thrones', 22.99, 60, 90, 2, 1),
                                                                                       ('978-0451524935', '1984', 13.99, 95, 120, 2, 2);

-- 7. Carts (mỗi user có 1 giỏ hàng)
INSERT INTO carts (user_id, created_at) VALUES
    (2, NOW());

-- 8. Cart items
INSERT INTO cart_items (cart_id, book_id, quantity) VALUES
                                                        (1, 1, 2),
                                                        (1, 2, 1);

-- 9. Orders
INSERT INTO orders (user_id, order_date, total_amount, status, shipping_address) VALUES
                                                                                     (2, DATE_SUB(NOW(), INTERVAL 5 DAY), 62.97, 'DELIVERED', '123 Main St, New York, NY 10001'),
                                                                                     (2, DATE_SUB(NOW(), INTERVAL 1 DAY), 22.99, 'PROCESSING', '123 Main St, New York, NY 10001');

-- 10. Order items
INSERT INTO order_items (order_id, book_id, quantity, price) VALUES
                                                                 (1, 1, 2, 19.99),
                                                                 (1, 2, 1, 22.99),
                                                                 (2, 2, 1, 22.99);

-- 11. Payments
INSERT INTO payments (order_id, payment_date, amount, method, status, transaction_id) VALUES
                                                                                          (1, DATE_SUB(NOW(), INTERVAL 5 DAY), 62.97, 'CREDIT_CARD', 'COMPLETED', 'TXN-001'),
                                                                                          (2, NOW(), 22.99, 'PAYPAL', 'PENDING', 'TXN-002');