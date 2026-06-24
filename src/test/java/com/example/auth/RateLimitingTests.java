package com.example.auth;

import static org.mockito.Mockito.when;
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

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class RateLimitingTests {

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
        // Setup operations mock
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    public void testRateLimitingBehavesCorrectly() throws Exception {
        String ipKey = "rate:limit:127.0.0.1";

        // Mock Redis count increment: first 10 requests increment from 1 to 10 (allowed)
        // The 11th request returns 11, which triggers the 429 block
        when(valueOperations.increment(ipKey))
                .thenReturn(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L);

        Map<String, String> payload = Map.of(
            "username", "testuser",
            "password", "password123"
        );
        String jsonContent = objectMapper.writeValueAsString(payload);

        // Perform the first 10 requests, which should run through the rate limit check successfully
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonContent)
                    .with(request -> {
                        request.setRemoteAddr("127.0.0.1"); // Enforce localhost loopback IP
                        return request;
                    }));
        }

        // The 11th request should hit the limit (count = 11 > LIMIT) and be blocked
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
                .with(request -> {
                    request.setRemoteAddr("127.0.0.1");
                    return request;
                }))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("Too Many Requests"))
                .andExpect(jsonPath("$.message").value("Rate limit exceeded. Maximum 10 requests per 60 seconds."));
    }
}
