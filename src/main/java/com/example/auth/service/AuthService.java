package com.example.auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.auth.security.JwtUtil;

@Service
public class AuthService {

    private final AuthenticationManager authManager;

    public AuthService(AuthenticationManager authManager) {
        this.authManager = authManager;
    }

    /**
     * Authenticate user and generate JWT token
     */
    public String login(String username, String password) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        // If no exception, authentication succeeded
        return JwtUtil.generateToken(username);
    }
}