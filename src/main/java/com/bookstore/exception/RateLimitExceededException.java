package com.bookstore.exception;

import lombok.Getter;

@Getter
public class RateLimitExceededException extends RuntimeException {

    private final long resetTime;

    public RateLimitExceededException(String message, long resetTime) {
        super(message);
        this.resetTime = resetTime;
    }

    public RateLimitExceededException(String message) {
        this(message, 60);
    }
}