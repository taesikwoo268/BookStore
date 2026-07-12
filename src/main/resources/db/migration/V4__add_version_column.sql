-- Thêm cột version vào bảng books
ALTER TABLE books ADD COLUMN version INT NOT NULL DEFAULT 0;