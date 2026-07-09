package com.bookstore.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ISBNValidator implements ConstraintValidator<ValidISBN, String> {

    @Override
    public void initialize(ValidISBN constraintAnnotation) {
        // Không cần khởi tạo
    }

    @Override
    public boolean isValid(String isbn, ConstraintValidatorContext context) {
        if (isbn == null || isbn.isBlank()) {
            return false;
        }

        // Loại bỏ dấu gạch ngang và khoảng trắng
        String cleanIsbn = isbn.replaceAll("[\\s-]", "");

        // Kiểm tra độ dài (ISBN-13 có 13 chữ số)
        if (cleanIsbn.length() != 13) {
            return false;
        }

        // Kiểm tra tất cả là chữ số
        if (!cleanIsbn.matches("\\d{13}")) {
            return false;
        }

        // Kiểm tra checksum
        return isValidISBN13(cleanIsbn);
    }

    private boolean isValidISBN13(String isbn) {
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(isbn.charAt(i));
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int checksum = (10 - (sum % 10)) % 10;
        int lastDigit = Character.getNumericValue(isbn.charAt(12));
        return checksum == lastDigit;
    }
}