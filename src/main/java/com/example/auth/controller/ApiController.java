package com.example.auth.controller;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {

    /**
     * Protected profile endpoint. Accessible by any authenticated user.
     */
    @GetMapping("/user/me")
    public ResponseEntity<?> getMyProfile(Authentication authentication) {
        if (authentication == null) {
            // Avoid silent fallback, warn if authentication is not populated
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized", "message", "User details not found in context"));
        }

        String username = authentication.getName();
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(", "));

        return ResponseEntity.ok(Map.of(
                "username", username,
                "role", roles,
                "status", "Active",
                "message", "Successfully retrieved secure profile data"
        ));
    }

    /**
     * Restricted administrative endpoint. Only accessible by users with ROLE_ADMIN.
     */
    @GetMapping("/admin")
    public ResponseEntity<?> getAdminStats() {
        return ResponseEntity.ok(Map.of(
                "roleRequired", "ROLE_ADMIN",
                "secretCode", "OWASP-TOP10-PROTECTED",
                "message", "Welcome Admin! You have accessed the secure enterprise administration API dashboard."
        ));
    }
}
