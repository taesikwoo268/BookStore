-- =====================================================
-- V3__seed_data.sql
-- Dữ liệu mẫu mở rộng cho BookStore (2 Roles, 20+ Authors, 40+ Books, Nhiều Orders/Payments)
-- =====================================================

-- 1. Roles
INSERT INTO roles (name) VALUES
                             ('ROLE_ADMIN'),
                             ('ROLE_USER');

-- 2. Users (Mật khẩu BCrypt mẫu cho tất cả: '123456')
INSERT INTO users (username, password, email, full_name, created_at) VALUES
                                                                         ('admin', '$2a$10$NkM5qS2Z5q2pJ.8hZ4Q.0uD6KJ9XbZ7XbZ7XbZ7XbZ7XbZ7XbZ7XbZ7', 'admin@bookstore.com', 'Administrator', NOW()),
                                                                         ('john_doe', '$2a$10$NkM5qS2Z5q2pJ.8hZ4Q.0uD6KJ9XbZ7XbZ7XbZ7XbZ7XbZ7XbZ7XbZ7', 'john.doe@email.com', 'John Doe', NOW()),
                                                                         ('jane_smith', '$2a$10$NkM5qS2Z5q2pJ.8hZ4Q.0uD6KJ9XbZ7XbZ7XbZ7XbZ7XbZ7XbZ7XbZ7', 'jane.smith@email.com', 'Jane Smith', NOW()),
                                                                         ('alice_w', '$2a$10$NkM5qS2Z5q2pJ.8hZ4Q.0uD6KJ9XbZ7XbZ7XbZ7XbZ7XbZ7XbZ7XbZ7', 'alice.w@email.com', 'Alice Wonder', NOW());

-- 3. User roles
INSERT INTO user_roles (user_id, role_id) VALUES
                                              (1, 1), -- admin có ROLE_ADMIN
                                              (1, 2), -- admin cũng có ROLE_USER
                                              (2, 2), -- john_doe có ROLE_USER
                                              (3, 2), -- jane_smith có ROLE_USER
                                              (4, 2); -- alice_w có ROLE_USER

-- 4. Authors (20 Tác giả nổi tiếng)
INSERT INTO authors (name, biography) VALUES
                                          ('J.K. Rowling', 'British author, best known for Harry Potter series.'),
                                          ('George R.R. Martin', 'American author of A Song of Ice and Fire.'),
                                          ('George Orwell', 'English novelist and essayist, famous for dystopian works.'),
                                          ('J.R.R. Tolkien', 'English writer, scholar, and philologist, author of The Lord of the Rings.'),
                                          ('Stephen King', 'American author of horror, supernatural fiction, and suspense.'),
                                          ('Agatha Christie', 'English writer known for her 66 detective novels and short story collections.'),
                                          ('Haruki Murakami', 'Japanese writer whose work has been translated into 50 languages.'),
                                          ('Dan Brown', 'American author of thriller novels, including the Robert Langdon series.'),
                                          ('Ernest Hemingway', 'American novelist, short-story writer, and journalist.'),
                                          ('F. Scott Fitzgerald', 'American novelist, widely regarded as one of the greatest American writers of the 20th century.'),
                                          ('Mark Twain', 'American writer, humorist, entrepreneur, publisher, and lecturer.'),
                                          ('Arthur Conan Doyle', 'British writer and physician, creator of Sherlock Holmes.'),
                                          ('Jane Austen', 'English novelist known primarily for her six major novels, which interpret the British landed gentry.'),
                                          ('Leo Tolstoy', 'Russian writer who is regarded as one of the greatest authors of all time.'),
                                          ('Fyodor Dostoevsky', 'Russian novelist, short story writer, essayist, and journalist.'),
                                          ('Isaac Asimov', 'American writer and professor of biochemistry, known for hard science fiction.'),
                                          ('Neil Gaiman', 'English author of short fiction, novels, comic books, and graphic novels.'),
                                          ('Rick Riordan', 'American author, best known for the Percy Jackson & the Olympians series.'),
                                          ('Gillian Flynn', 'American author, comic book writer, and former television critic.'),
                                          ('Yuval Noah Harari', 'Israeli public intellectual, historian and professor.');

-- 5. Categories (10 Danh mục sách đa dạng)
INSERT INTO categories (name, description) VALUES
                                               ('Fantasy', 'Books about magic, mythical creatures, and imaginary worlds.'),
                                               ('Science Fiction', 'Books about future technology, space exploration, and time travel.'),
                                               ('Dystopian', 'Books exploring social and political structures in a dark, nightmare world.'),
                                               ('Mystery & Thriller', 'Suspenseful stories involving crimes, puzzles, and intense plots.'),
                                               ('Classic Literature', 'Time-tested masterpieces of world literature.'),
                                               ('Horror', 'Fiction designed to frighten, scare, or startle readers.'),
                                               ('Historical Fiction', 'Stories set in the past, blending historical facts with fictional characters.'),
                                               ('Biography & History', 'Non-fiction books about real people''s lives and historical events.'),
                                               ('Adventure & Mythology', 'Exciting journeys, heroic deeds, and mythological adaptations.'),
                                               ('Philosophy & Science', 'Non-fiction exploring deep human thought, society, and scientific concepts.');

-- 6. Books (45 Cuốn sách phân bổ đều các Category và Author)
INSERT INTO books (isbn, title, price, stock, sales_count, author_id, category_id) VALUES
-- Fantasy (Category 1)
('978-0439708184', 'Harry Potter and the Sorcerer''s Stone', 19.99, 100, 150, 1, 1),
('978-0439064873', 'Harry Potter and the Chamber of Secrets', 20.99, 85, 110, 1, 1),
('978-0553103540', 'A Game of Thrones', 22.99, 60, 90, 2, 1),
('978-0553108033', 'A Clash of Kings', 24.99, 45, 75, 2, 1),
('978-0618640157', 'The Lord of the Rings: The Fellowship of the Ring', 25.00, 70, 200, 4, 1),
('978-0618260256', 'The Hobbit', 14.99, 120, 180, 4, 1),
('978-0385603409', 'American Gods', 16.99, 50, 65, 17, 1),

-- Science Fiction (Category 2)
('978-0451450523', 'I, Robot', 12.99, 80, 95, 16, 2),
('978-0553293357', 'Foundation', 15.99, 65, 85, 16, 2),
('978-0062059888', 'The Ocean at the End of the Lane', 13.50, 40, 45, 17, 2),

-- Dystopian (Category 3)
('978-0451524935', '1984', 13.99, 95, 120, 3, 3),
('978-0452284234', 'Animal Farm', 9.99, 150, 310, 3, 3),

-- Mystery & Thriller (Category 4)
('978-0007119318', 'And Then There Were None', 11.99, 110, 250, 6, 4),
('978-0007120598', 'Murder on the Orient Express', 10.99, 90, 190, 6, 4),
('978-0307474278', 'The Da Vinci Code', 16.99, 140, 420, 8, 4),
('978-0307474261', 'Angels & Demons', 15.99, 100, 280, 8, 4),
('978-0061732188', 'Sherlock Holmes: A Study in Scarlet', 8.99, 75, 130, 12, 4),
('978-0224087117', 'Gone Girl', 14.99, 60, 175, 19, 4),

-- Classic Literature (Category 5)
('978-0684801469', 'The Old Man and the Sea', 12.00, 85, 140, 9, 5),
('978-0684830490', 'A Farewell to Arms', 14.50, 55, 80, 9, 5),
('978-0743273565', 'The Great Gatsby', 15.00, 200, 500, 10, 5),
('978-0141439518', 'Pride and Prejudice', 7.99, 130, 340, 13, 5),
('978-0140449174', 'War and Peace', 19.99, 40, 60, 14, 5),
('978-0140449136', 'Anna Karenina', 16.00, 50, 90, 14, 5),
('978-0140449129', 'Crime and Punishment', 13.99, 70, 115, 15, 5),
('978-0140449228', 'The Brothers Karamazov', 17.50, 45, 70, 15, 5),

-- Horror (Category 6)
('978-1501142970', 'It', 19.99, 65, 210, 5, 6),
('978-0307743657', 'The Shining', 15.99, 80, 195, 5, 6),
('978-1501144264', 'Misery', 16.50, 50, 110, 5, 6),

-- Historical Fiction (Category 7)
('978-0345384751', 'The Adventures of Tom Sawyer', 6.99, 110, 150, 11, 7),
('978-0451526342', 'Adventures of Huckleberry Finn', 7.99, 95, 165, 11, 7),
('978-1400079988', 'Kafka on the Shore', 16.95, 55, 130, 7, 7),
('978-0375712326', 'Norwegian Wood', 15.95, 75, 240, 7, 7),

-- Biography & History (Category 8)
('978-0062316097', 'Sapiens: A Brief History of Humankind', 22.99, 180, 600, 20, 8),
('978-0062560346', 'Homo Deus: A Brief History of Tomorrow', 24.99, 120, 340, 20, 8),
('978-0517223338', 'The Complete Sherlock Holmes', 29.99, 35, 80, 12, 8),

-- Adventure & Mythology (Category 9)
('978-0786838653', 'The Lightning Thief', 9.99, 140, 450, 18, 9),
('978-0786851416', 'The Sea of Monsters', 9.99, 110, 280, 18, 9),
('978-0786851430', 'The Titans Curse', 10.99, 100, 260, 18, 9),

-- Philosophy & Science (Category 10)
('978-0521637220', 'The Republic of Plato', 11.99, 60, 75, 14, 10),
('978-0140441031', 'Thus Spoke Zarathustra', 12.99, 45, 50, 15, 10),
('978-0062851598', '21 Lessons for the 21st Century', 21.99, 90, 190, 20, 10),
('978-0307947390', 'Wind-Up Bird Chronicle', 17.00, 40, 85, 7, 10),
('978-1101911761', 'The Beautiful and Damned', 13.00, 30, 40, 10, 10),
('978-1501143519', 'Pet Sematary', 16.99, 70, 145, 5, 6);

-- 7. Carts (Mỗi user 2, 3, 4 có 1 giỏ hàng)
INSERT INTO carts (user_id, created_at) VALUES
                                            (2, NOW()),
                                            (3, DATE_SUB(NOW(), INTERVAL 1 DAY)),
                                            (4, NOW());

-- 8. Cart items (Phân bổ đồ vật vào các giỏ hàng)
INSERT INTO cart_items (cart_id, book_id, quantity) VALUES
                                                        (1, 1, 2),  -- Giỏ hàng 1 có 2 quyển Harry Potter 1
                                                        (1, 3, 1),  -- Giỏ hàng 1 có 1 quyển Game of Thrones
                                                        (2, 21, 1), -- Giỏ hàng 2 có 1 quyển The Great Gatsby
                                                        (2, 34, 1), -- Giỏ hàng 2 có 1 quyển Sapiens
                                                        (3, 27, 2); -- Giỏ hàng 3 có 2 quyển IT (Stephen King)

-- 9. Orders (Lịch sử mua hàng phong phú hơn)
INSERT INTO orders (user_id, order_date, total_amount, status, shipping_address) VALUES
                                                                                     (2, DATE_SUB(NOW(), INTERVAL 5 DAY), 62.97, 'DELIVERED', '123 Main St, New York, NY 10001'),
                                                                                     (2, DATE_SUB(NOW(), INTERVAL 1 DAY), 22.99, 'PROCESSING', '123 Main St, New York, NY 10001'),
                                                                                     (3, DATE_SUB(NOW(), INTERVAL 10 DAY), 37.99, 'DELIVERED', '456 Oak Ave, Los Angeles, CA 90001'),
                                                                                     (4, DATE_SUB(NOW(), INTERVAL 2 DAY), 48.97, 'SHIPPED', '789 Pine Rd, San Francisco, CA 94101');

-- 10. Order items
INSERT INTO order_items (order_id, book_id, quantity, price) VALUES
-- Đơn hàng 1
(1, 1, 2, 19.99),
(1, 3, 1, 22.99),
-- Đơn hàng 2
(2, 3, 1, 22.99),
-- Đơn hàng 3
(3, 11, 1, 13.99),
(3, 5, 1, 24.00),
-- Đơn hàng 4
(4, 37, 1, 9.99),
(4, 27, 1, 19.99),
(4, 31, 1, 18.99);

-- 11. Payments (Tương ứng với các trạng thái đơn hàng)
INSERT INTO payments (order_id, payment_date, amount, method, status, transaction_id) VALUES
                                                                                          (1, DATE_SUB(NOW(), INTERVAL 5 DAY), 62.97, 'CREDIT_CARD', 'COMPLETED', 'TXN-001'),
                                                                                          (2, NOW(), 22.99, 'PAYPAL', 'PENDING', 'TXN-002'),
                                                                                          (3, DATE_SUB(NOW(), INTERVAL 10 DAY), 37.99, 'BANK_TRANSFER', 'COMPLETED', 'TXN-003'),
                                                                                          (4, DATE_SUB(NOW(), INTERVAL 2 DAY), 48.97, 'CREDIT_CARD', 'COMPLETED', 'TXN-004');