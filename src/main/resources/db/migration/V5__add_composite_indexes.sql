-- =====================================================
-- V5__add_composite_indexes.sql
-- Thêm composite indexes để tối ưu hiệu suất truy vấn
-- =====================================================

-- ===== 1. COMPOSITE INDEXES =====

-- 1.1. Index cho tìm kiếm sách theo giá và sales_count
CREATE INDEX idx_books_price_sales ON books(price, sales_count);

-- 1.2. Index cho lọc sách theo category (bảng trung gian)
CREATE INDEX idx_book_categories_composite ON book_categories(book_id, category_id);

-- 1.3. Index cho đơn hàng theo user và status
CREATE INDEX idx_orders_user_status ON orders(user_id, status);

-- 1.4. Index cho đơn hàng theo user và order_date
CREATE INDEX idx_orders_user_date ON orders(user_id, order_date);

-- 1.5. Index cho order_items theo order và book
CREATE INDEX idx_order_items_order_book ON order_items(order_id, book_id);

-- 1.6. Index cho cart_items theo cart và book
CREATE INDEX idx_cart_items_cart_book ON cart_items(cart_id, book_id);

-- ===== 2. COVERING INDEXES =====

-- 2.1. Covering index cho BookSummary query
CREATE INDEX idx_books_cover_summary ON books(id, title, price, stock, sales_count, author_id);

-- 2.2. Covering index cho Book search
CREATE INDEX idx_books_cover_search ON books(title, isbn, price, stock);

-- ===== 3. FULLTEXT INDEXES =====

-- 3.1. Fulltext index cho tìm kiếm sách theo title
ALTER TABLE books ADD FULLTEXT INDEX ft_books_title(title);

-- 3.2. Fulltext index cho tìm kiếm sách theo title và isbn
ALTER TABLE books ADD FULLTEXT INDEX ft_books_title_isbn(title, isbn);

-- ===== 4. KIỂM TRA INDEXES =====
-- SELECT
--     TABLE_NAME,
--     INDEX_NAME,
--     COLUMN_NAME,
--     NON_UNIQUE,
--     INDEX_TYPE
-- FROM information_schema.STATISTICS
-- WHERE TABLE_SCHEMA = 'bookstore'
-- ORDER BY TABLE_NAME, INDEX_NAME;