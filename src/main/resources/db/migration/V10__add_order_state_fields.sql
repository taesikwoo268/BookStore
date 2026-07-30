-- ============================================================
-- V10__add_order_state_fields.sql
-- Thêm các cột tracking cho từng trạng thái đơn hàng
-- ============================================================

ALTER TABLE orders
    ADD COLUMN confirmed_at      TIMESTAMP    NULL COMMENT 'Thời gian xác nhận',
    ADD COLUMN processing_at     TIMESTAMP    NULL COMMENT 'Thời gian xử lý',
    ADD COLUMN cancelled_at      TIMESTAMP    NULL COMMENT 'Thời gian hủy',
    ADD COLUMN cancelled_reason  VARCHAR(255) NULL COMMENT 'Lý do hủy',
    ADD COLUMN is_auto_cancelled BOOLEAN DEFAULT FALSE COMMENT 'Hủy tự động bởi scheduled job',
    ADD COLUMN shipped_at        TIMESTAMP    NULL COMMENT 'Thời gian giao hàng',
    ADD COLUMN delivered_at      TIMESTAMP    NULL COMMENT 'Thời gian nhận hàng',
    ADD COLUMN refunded_at       TIMESTAMP    NULL COMMENT 'Thời gian hoàn tiền';

-- Đảm bảo status có thể lưu giá trị đúng
-- ALTER TABLE orders MODIFY status VARCHAR(20);