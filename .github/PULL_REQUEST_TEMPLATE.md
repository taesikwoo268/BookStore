# 📝 Pull Request

## 📋 Mô tả
<!-- Mô tả ngắn gọn về thay đổi này và lý do cần thiết -->

## 🔗 Issue liên quan
<!-- Link issue nếu có -->
- Closes #(issue number)

## 🎯 Loại thay đổi
- [ ] 🐛 Bug fix (sửa lỗi)
- [ ] ✨ New feature (tính năng mới)
- [ ] 💥 Breaking change (thay đổi phá vỡ compatibility)
- [ ] 📚 Documentation (cập nhật tài liệu)
- [ ] 🔧 Refactor (tái cấu trúc code)
- [ ] ⚡ Performance (tối ưu hiệu năng)
- [ ] 🧪 Test (thêm test)
- [ ] 🔒 Security (bảo mật)

## 🧪 Kiểm thử
<!-- Mô tả cách test thay đổi này -->

### ✅ Test tự động
- [ ] Unit tests pass
- [ ] Integration tests pass
- [ ] Code coverage không giảm

### ✅ Test thủ công
- [ ] Chạy ứng dụng local
- [ ] Kiểm tra API bằng Postman/Swagger
- [ ] Kiểm tra UI (nếu có)

## 📸 Screenshots (nếu có)
<!-- Thêm ảnh chụp màn hình nếu cần -->

## 📝 Checklist

### 📋 Code Quality
- [ ] Code tuân thủ coding convention
- [ ] Không có code không dùng (unused imports, dead code)
- [ ] Không có warnings trong IDE
- [ ] Có log đầy đủ cho các operation quan trọng

### 🗄️ Database
- [ ] Migration được tạo (nếu có thay đổi schema)
- [ ] Migration đã được test
- [ ] Seed data được cập nhật (nếu cần)

### 🔒 Security
- [ ] Không expose sensitive data (password, token, key)
- [ ] Có validation cho input từ client
- [ ] Có permission/role check (nếu cần)

### 📚 Documentation
- [ ] API được document trên Swagger/OpenAPI
- [ ] README được cập nhật (nếu cần)
- [ ] Có comment cho code phức tạp

### ⚡ Performance
- [ ] Không có N+1 query
- [ ] Cache được sử dụng hợp lý (nếu cần)
- [ ] API response time < 500ms (cho GET)

### 🔧 Build & Deploy
- [ ] Build thành công (mvn clean compile)
- [ ] Test pass (mvn test)
- [ ] Docker image build thành công (nếu có)

## 📋 Kiểm tra của Reviewer

### Reviewer Checklist
- [ ] Code có dễ đọc không?
- [ ] Có đủ test coverage không?
- [ ] Có break API không?
- [ ] Có ảnh hưởng đến performance không?
- [ ] Có vấn đề bảo mật không?
- [ ] Database migration có an toàn không?

## 📝 Ghi chú bổ sung
<!-- Thêm bất kỳ thông tin nào khác -->

---

## 🔄 Checklist nhanh cho PR

### Developer
- [ ] Code đã được test
- [ ] Unit tests pass
- [ ] Integration tests pass
- [ ] Không có warning
- [ ] Log đầy đủ
- [ ] Migration test
- [ ] Build thành công

### Reviewer
- [ ] Code review
- [ ] Test pass
- [ ] Approval

### Sau khi merge
- [ ] Deploy thành công
- [ ] Monitoring
- [ ] Notification