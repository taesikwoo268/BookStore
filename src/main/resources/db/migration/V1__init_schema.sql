-- Bảng users (tích hợp với Spring Security nếu có)
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       full_name VARCHAR(100),
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng roles
CREATE TABLE roles (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(50) NOT NULL UNIQUE
);

-- Bảng user_roles (liên kết nhiều-nhiều)
CREATE TABLE user_roles (
                            user_id BIGINT,
                            role_id BIGINT,
                            PRIMARY KEY (user_id, role_id),
                            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                            FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Bảng authors
CREATE TABLE authors (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         name VARCHAR(100) NOT NULL,
                         biography TEXT
);

-- Bảng categories
CREATE TABLE categories (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            name VARCHAR(50) NOT NULL UNIQUE,
                            description TEXT
);

-- Bảng books
CREATE TABLE books (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       isbn VARCHAR(20) NOT NULL UNIQUE,
                       title VARCHAR(200) NOT NULL,
                       price DECIMAL(10,2),
                       stock INT,
                       sales_count INT DEFAULT 0,
                       author_id BIGINT,
                       category_id BIGINT,
                       FOREIGN KEY (author_id) REFERENCES authors(id),
                       FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- Bảng carts (giỏ hàng của user)
CREATE TABLE carts (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       user_id BIGINT NOT NULL UNIQUE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Bảng cart_items
CREATE TABLE cart_items (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            cart_id BIGINT NOT NULL,
                            book_id BIGINT NOT NULL,
                            quantity INT NOT NULL,
                            FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
                            FOREIGN KEY (book_id) REFERENCES books(id)
);

-- Bảng orders
CREATE TABLE orders (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        user_id BIGINT NOT NULL,
                        order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        total_amount DECIMAL(10,2),
                        status VARCHAR(20),
                        shipping_address TEXT,
                        FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Bảng order_items
CREATE TABLE order_items (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             order_id BIGINT NOT NULL,
                             book_id BIGINT NOT NULL,
                             quantity INT NOT NULL,
                             price DECIMAL(10,2),
                             FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                             FOREIGN KEY (book_id) REFERENCES books(id)
);

-- Bảng payments
CREATE TABLE payments (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          order_id BIGINT NOT NULL,
                          payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          amount DECIMAL(10,2),
                          method VARCHAR(50),
                          status VARCHAR(20),
                          transaction_id VARCHAR(100),
                          FOREIGN KEY (order_id) REFERENCES orders(id)
);