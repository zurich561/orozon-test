package com.example.orzon_example.product;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SearchRateLimiter {

    private final Map<String, Instant> lastRequest = new ConcurrentHashMap<>();
    private final Duration throttleWindow = Duration.ofMillis(400);

    public void verifyRequestAllowed(String key) {
        Instant now = Instant.now();
        Instant previous = lastRequest.put(key, now);
        if (previous != null && Duration.between(previous, now).compareTo(throttleWindow) < 0) {
            throw new TooManySearchRequestsException();
        }
    }

    public static class TooManySearchRequestsException extends RuntimeException {
        public TooManySearchRequestsException() {
            super("Too many search requests. Please slow down.");
        }
    }
}
