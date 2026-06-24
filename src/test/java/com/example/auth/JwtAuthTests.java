package com.example.auth;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.auth.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class JwtAuthTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StringRedisTemplate redisTemplate;

    @MockBean
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    public void setUp() {
        // Setup mock behavior for StringRedisTemplate
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    public void testSuccessfulLogin() throws Exception {
        Map<String, String> payload = Map.of(
            "username", "testuser",
            "password", "password123"
        );

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    public void testLoginWithInvalidCredentials() throws Exception {
        Map<String, String> payload = Map.of(
            "username", "testuser",
            "password", "wrongpassword"
        );

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    public void testAccessProfileWithValidTokenAndSession() throws Exception {
        String token = JwtUtil.generateToken("testuser");

        // Mock Redis session check: token exists in Redis
        when(valueOperations.get("session:token:" + token)).thenReturn("testuser");

        mockMvc.perform(get("/api/user/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
    }

    @Test
    public void testAccessProfileWithValidTokenButLoggedOutSession() throws Exception {
        String token = JwtUtil.generateToken("testuser");

        // Mock Redis session check: token does not exist in Redis (logged out)
        when(valueOperations.get("session:token:" + token)).thenReturn(null);

        mockMvc.perform(get("/api/user/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Session is invalid or logged out."));
    }

    @Test
    public void testAdminAccessRestrictedToRoleAdmin() throws Exception {
        String userToken = JwtUtil.generateToken("testuser");
        String adminToken = JwtUtil.generateToken("admin");

        // Mock Redis sessions
        when(valueOperations.get("session:token:" + userToken)).thenReturn("testuser");
        when(valueOperations.get("session:token:" + adminToken)).thenReturn("admin");

        // 1. User role access admin -> Forbidden 403
        mockMvc.perform(get("/api/admin")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        // 2. Admin role access admin -> Success 200
        mockMvc.perform(get("/api/admin")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.secretCode").value("OWASP-TOP10-PROTECTED"));
    }
}
