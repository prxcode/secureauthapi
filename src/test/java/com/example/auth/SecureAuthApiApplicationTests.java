package com.example.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
class SecureAuthApiApplicationTests {

    @MockBean
    private StringRedisTemplate redisTemplate; // Mock redis so application loads without active server

    @Test
    void contextLoads() {
        // Verifies Spring context initialized correctly
    }
}
