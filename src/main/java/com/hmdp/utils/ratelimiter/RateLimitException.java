package com.hmdp.utils.ratelimiter;

public class RateLimitException extends RuntimeException {
    public RateLimitException(String message) {
        super(message);
    }
}