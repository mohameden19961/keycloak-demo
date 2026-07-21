package com.testing.test.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Order(1)
public class RateLimitingFilter implements Filter {

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private static final long MAX_REQUESTS = 100;
    private static final long WINDOW_MILLIS = 60_000;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String key = req.getRemoteAddr();
        TokenBucket bucket = buckets.computeIfAbsent(key, k -> new TokenBucket(MAX_REQUESTS, WINDOW_MILLIS));

        if (!bucket.tryConsume()) {
            res.setStatus(429);
            res.setContentType("application/json");
            res.getWriter().write("{\"error\":\"Trop de requêtes. Réessayez dans 60 secondes.\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    private static class TokenBucket {
        private final long maxTokens;
        private final long windowMillis;
        private final AtomicLong tokens;
        private volatile long lastRefill;

        TokenBucket(long maxTokens, long windowMillis) {
            this.maxTokens = maxTokens;
            this.windowMillis = windowMillis;
            this.tokens = new AtomicLong(maxTokens);
            this.lastRefill = System.currentTimeMillis();
        }

        boolean tryConsume() {
            refill();
            long current = tokens.get();
            if (current <= 0) return false;
            return tokens.compareAndSet(current, current - 1);
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefill;
            if (elapsed > windowMillis) {
                tokens.set(maxTokens);
                lastRefill = now;
            } else {
                long toAdd = (elapsed * maxTokens) / windowMillis;
                long current = tokens.get();
                long newValue = Math.min(maxTokens, current + toAdd);
                if (newValue > current) {
                    tokens.compareAndSet(current, newValue);
                    lastRefill = now;
                }
            }
        }
    }
}
