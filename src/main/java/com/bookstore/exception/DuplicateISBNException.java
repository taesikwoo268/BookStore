package com.bookstore.exception;

public class DuplicateISBNException extends RuntimeException {
    public DuplicateISBNException(String message) {
        super(message);
    }
}