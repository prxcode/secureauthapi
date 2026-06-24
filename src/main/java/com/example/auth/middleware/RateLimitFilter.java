package com.example.auth.middleware;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Component
@Order(2) // Runs second (after IP Filter)
public class RateLimitFilter implements Filter {

    private final StringRedisTemplate redisTemplate;

    // Rate limit configuration: 10 requests per minute
    private static final int LIMIT = 10;
    private static final int WINDOW_SECONDS = 60;

    public RateLimitFilter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();

        // Rate limit authentication and API endpoints only
        if (path.startsWith("/api/") || path.startsWith("/auth/")) {
            String ip = httpRequest.getRemoteAddr();
            String redisKey = "rate:limit:" + ip;

            try {
                // Increment request counter in Redis
                Long count = redisTemplate.opsForValue().increment(redisKey);

                if (count == null) {
                    throw new RuntimeException("Redis counter increment returned null");
                }

                // If first request in this window, set TTL
                if (count == 1) {
                    redisTemplate.expire(redisKey, Duration.ofSeconds(WINDOW_SECONDS));
                }

                // Check if rate limit exceeded
                if (count > LIMIT) {
                    httpResponse.setStatus(429);
                    httpResponse.setContentType("application/json");
                    httpResponse.getWriter().write("{\"error\": \"Too Many Requests\", \"message\": \"Rate limit exceeded. Maximum " + LIMIT + " requests per " + WINDOW_SECONDS + " seconds.\"}");
                    return;
                }
            } catch (Exception e) {
                // Avoid silent fallbacks: if Redis is down, return a clear 500 server error
                httpResponse.setStatus(500);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write("{\"error\": \"Internal Server Error\", \"message\": \"Rate limiting service is down. Connection error: " + e.getMessage() + "\"}");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}