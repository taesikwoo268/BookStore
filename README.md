# 📚 BookStore Management System

Một ứng dụng **RESTful API Backend** xây dựng trên nền tảng **Spring Boot 3**, cung cấp giải pháp toàn diện cho hệ thống quản lý cửa hàng sách trực tuyến. Hệ thống hỗ trợ xác thực JWT/RBAC mã hóa RSA, xử lý sự kiện đặt hàng bất đồng bộ qua RabbitMQ, cơ chế Idempotency tránh trùng lặp giao dịch và hỗ trợ đa môi trường cơ sở dữ liệu (MySQL / H2 In-Memory).

---

## 🛠️ Công nghệ sử dụng

- **Core Framework:** Java 17, Spring Boot 3.3.0
- **Database & Migration:** MySQL 8.x, H2 Database (Test Environment), Flyway Migration
- **Security:** Spring Security, JWT (JSON Web Token), RSA Public/Private Key Provider, Role-Based Access Control (RBAC)
- **Message Broker:** RabbitMQ (Event-Driven Architecture)
- **Mapping & Tooling:** MapStruct, Lombok, Spring Validation, Spring Retry, AspectJ (AOP)
- **Documentation:** SpringDoc OpenAPI (Swagger UI)
- **Testing:** JUnit 5, Mockito, AssertJ, Spring MockMvc
- **Containerization:** Docker, Docker Compose

---

## 🏗️ Cấu trúc dự án

```text
src/
├── main/
│   ├── java/com/bookstore/
│   │   ├── aspect/          # AOP Logging (LoggingAspect)
│   │   ├── config/          # SecurityConfig, OpenAPIConfig, RabbitMQConfig
│   │   ├── controller/      # REST Endpoints (Auth, Book, Author, Category, Cart, Checkout)
│   │   ├── dto/             # Data Transfer Objects (Request/Response)
│   │   ├── enums/           # OrderStatus, PaymentMethod, PaymentStatus
│   │   ├── exception/       # GlobalExceptionHandler & Custom Exceptions
│   │   ├── mapper/          # Entity-DTO Mappers (BookMapper, AuthorMapper, CartMapper,...)
│   │   ├── model/           # JPA Entities (Book, Order, User, Role, IdempotencyKey,...)
│   │   ├── repository/      # Spring Data JPA Repositories
│   │   ├── security/        # JwtAuthenticationFilter, JwtRsaProvider, CustomUserDetails
│   │   ├── service/         # Business Logic (BookService, OrderService, AuthService, StockService,...)
│   │   └── validation/      # Custom Input Validators (ValidISBN, ISBNValidator, BookValidator)
│   └── resources/
│       ├── application.properties
│       └── db/migration/    # Flyway SQL Migration Scripts (V1__ -> V8__)
└── test/                    # Unit Tests & Concurrency / Integration Tests
```

---

## ✨ Tính năng hệ thống

### 1. Xác thực & Phân quyền (Authentication & Authorization)
* **Đăng ký / Đăng nhập:** Mã hóa mật khẩu bằng `PasswordEncoder` (BCrypt), phát hành cặp Access Token và Refresh Token.
* **JWT với RSA:** Sử dụng thuật toán bất đối xứng RSA để ký và xác thực token thông qua `JwtRsaProvider`.
* **Phân quyền RBAC:** Phân quyền truy cập tài nguyên chi tiết theo vai trò (`ROLE_USER`, `ROLE_ADMIN`) và permission gắn kèm.

### 2. Quản lý Sách & Danh mục (Book & Category Management)
* **CRUD Sách & Danh mục:** Thêm, sửa, xóa, tìm kiếm sách theo từ khóa, lọc sách theo khoảng giá.
* **Validation ISBN:** Tích hợp bộ kiểm tra định dạng và Checksum cho mã **ISBN-13** chuẩn quốc tế.
* **Xử lý tranh chấp dữ liệu (Optimistic Locking):** Áp dụng thuộc tính `version` trong Entity `Book` để phòng chống xung đột khi nhiều giao dịch cập nhật kho hàng cùng một thời điểm.

### 3. Giỏ hàng & Đặt hàng (Cart & Order Processing)
* **Quản lý Giỏ hàng:** Thêm, cập nhật số lượng, xóa sản phẩm khỏi giỏ hàng của từng người dùng.
* **Cơ chế chống tạo trùng đơn (Idempotency):** API `/api/v1/checkout` hỗ trợ nhận Header `Idempotency-Key`. Nếu client bấm thanh toán nhiều lần hoặc bị gián đoạn kết nối, hệ thống sẽ trả về ngay kết quả đơn hàng đã tạo trước đó mà không trừ kho/tạo đơn lần 2.
* **Giao dịch Kho an toàn:** Xử lý trừ tồn kho kết hợp `@Retryable` tự động thử lại khi gặp lỗi `OptimisticLockingFailureException`.

### 4. Kiến trúc Hướng sự kiện (Event-Driven với RabbitMQ)
* Bắn sự kiện ngay sau khi đơn hàng tạo thành công lên `order.exchange` (TopicExchange).
* Tự động điều hướng dữ liệu đến các Queue riêng biệt:
  * `inventoryQueue`: Cập nhật kho hàng / Thống kê bán hàng.
  * `emailQueue`: Gửi email xác nhận đơn hàng cho khách.
  * `notificationQueue`: Gửi thông báo hệ thống.
* **Dead Letter Queue (DLQ):** Tích hợp `order.dead-letter.queue` để hứng và lưu trữ các tin nhắn xử lý thất bại nhằm phục vụ retry hoặc debug.

---

## ⚙️ Cấu hình Môi trường (`application.properties`)

Dưới đây là mẫu cấu hình chính cho ứng dụng Spring Boot:

```properties
spring.application.name=BookStore

# Database Configuration (MySQL)
spring.datasource.url=jdbc:mysql://localhost:3306/bookstore?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# Flyway Migration
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:db/migration

# RabbitMQ Configuration
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

rabbitmq.exchange.order=order.exchange
rabbitmq.queue.order-placed=order.placed.queue
rabbitmq.queue.inventory=order.inventory.queue
rabbitmq.queue.email=order.email.queue
rabbitmq.queue.notification=order.notification.queue
rabbitmq.routing-key.order-placed=order.placed
```

---

## 🚀 Hướng dẫn Cài đặt & Khởi chạy

### 1. Yêu cầu môi trường
* **JDK:** Java 17 trở lên
* **Maven:** 3.8+ (hoặc dùng Wrapper `./mvnw`)
* **Docker & Docker Desktop**

### 2. Khởi chạy Docker Containers (MySQL & RabbitMQ)

Chạy lệnh Docker Compose để dựng các dịch vụ phụ thuộc:

```bash
docker compose up -d
```

Trạng thái dịch vụ:
* **MySQL:** `localhost:3306` (Database: `bookstore`)
* **RabbitMQ Management Console:** `http://localhost:15672` (Tài khoản: `guest` / `guest`)

### 3. Khởi chạy ứng dụng Spring Boot

Mở Terminal tại thư mục gốc dự án và gõ lệnh:

```bash
# Trên Windows
mvnw.cmd spring-boot:run

# Trên Linux / macOS
./mvnw spring-boot:run
```

Sau khi ứng dụng khởi động thành công:
* **Swagger UI Documentation:** `http://localhost:8080/swagger-ui.html`
* **OpenAPI Schema:** `http://localhost:8080/v3/api-docs`

---

## 🧪 Chạy Kiểm thử (Testing)

Dự án bao phủ kiểm thử đơn vị (Unit Test), kiểm thử đồng thời (Concurrency Test) và kiểm thử tích hợp REST API (MockMvc) chạy trên **H2 In-Memory Database**:

```bash
# Chạy toàn bộ các bộ Test
./mvnw test
```

### Danh sách các bộ test chính:
* `BookServiceTest`: Kiểm thử nghiệp vụ quản lý sách, kiểm tra ISBN trùng lặp, xử lý thiếu thông tin Tác giả/Thể loại.
* `AuthServiceTest`: Kiểm thử đăng ký, mã hóa mật khẩu, tạo Access Token & Refresh Token.
* `BookControllerTest`: Kiểm thử các endpoint REST API với MockMvc.
* `BookOptimisticLockTest`: Kiểm thử ghi dữ liệu đồng thời với Optimistic Locking.
* `OrderConcurrencyTest`: Kiểm thử xử lý tranh chấp tồn kho khi nhiều người dùng đặt hàng cùng lúc.

---

## 📋 Danh sách API Endpoints Chính

| HTTP Method | Endpoint | Mô tả | Authorization |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register` | Đăng ký tài khoản người dùng | Public |
| `POST` | `/api/v1/auth/login` | Đăng nhập & Lấy JWT Token | Public |
| `POST` | `/api/v1/auth/refresh-token` | Lấy Access Token mới từ Refresh Token | Public |
| `GET` | `/api/v1/books` | Lấy danh sách sách (Hỗ trợ lọc & tìm kiếm) | Public |
| `GET` | `/api/v1/books/{id}` | Lấy thông tin chi tiết một cuốn sách | Public |
| `POST` | `/api/v1/books` | Thêm sách mới vào hệ thống | Bearer Token (`ROLE_ADMIN`) |
| `PUT` | `/api/v1/books/{id}` | Cập nhật thông tin sách | Bearer Token (`ROLE_ADMIN`) |
| `DELETE` | `/api/v1/books/{id}` | Xóa sách khỏi hệ thống | Bearer Token (`ROLE_ADMIN`) |
| `POST` | `/api/v1/cart/items` | Thêm sản phẩm vào giỏ hàng | Bearer Token (`ROLE_USER`) |
| `POST` | `/api/v1/checkout` | Thực hiện thanh toán & tạo đơn hàng | Bearer Token (`ROLE_USER`), `Idempotency-Key` (Header) |
