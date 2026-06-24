package com.example.auth.service;

import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.auth.security.JwtUtil;

@Service
public class AuthService {

    private final AuthenticationManager authManager;
    private final StringRedisTemplate redisTemplate;

    public AuthService(AuthenticationManager authManager, StringRedisTemplate redisTemplate) {
        this.authManager = authManager;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Authenticate user, generate JWT, and save session in Redis.
     * Throws exception if Redis or DB is down.
     */
    public String login(String username, String password) {
        // Authenticate credentials against UserDetailsService
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        // If authentication succeeded, generate JWT token
        String token = JwtUtil.generateToken(username);

        // Save session mapping in Redis (session:token:<JWT> -> username) with 1-day TTL
        // If Redis is down, this will throw an exception to be handled by the controller
        redisTemplate.opsForValue().set("session:token:" + token, username, Duration.ofHours(24));

        return token;
    }

    /**
     * Terminate session by deleting the token key from Redis
     */
    public void logout(String token) {
        // Remove token from Redis cache
        // If Redis is down, this will throw an exception and let caller know
        redisTemplate.delete("session:token:" + token);
    }
}