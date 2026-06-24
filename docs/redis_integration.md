# Redis Integration: Rate Limiting & Session Management

SecureAuthAPI integrates Redis as a high-speed, in-memory data store for request rate-limiting and active session verification.

---

## Redis-Backed Rate Limiting

The [RateLimitFilter.java](file:///src/main/java/com/example/auth/middleware/RateLimitFilter.java) utilizes Redis to enforce request throttling.

### Algorithm (Fixed Window Counter)
- **Key Schema**: `rate:limit:<Client-IP>`
- **Threshold**: 10 requests
- **Window Size**: 60 seconds

### Execution Logic:
1. For every request targeting `/api/**` or `/auth/**`, the filter extracts the client's IP address.
2. It calls `redisTemplate.opsForValue().increment(key)`.
3. If the resulting count is `1`, the key is fresh. The filter sets a `60-second` expiration.
4. If the count exceeds `10`, the request is blocked, returning a `429 Too Many Requests` status code.
5. Once the 60-second TTL expires, the key is automatically evicted by Redis, resetting the window.

```java
Long count = redisTemplate.opsForValue().increment(redisKey);
if (count == 1) {
    redisTemplate.expire(redisKey, Duration.ofSeconds(60));
}
if (count > 10) {
    // Block request
}
```

---

## Redis-Backed Session Control (Token Revocation)

While JWTs are standard for stateless setups, they suffer from a primary weakness: **they cannot easily be revoked before expiration** without complex state management. 

SecureAuthAPI solves this using a **Redis Session Whitelist** pattern in [AuthService.java](file:///src/main/java/com/example/auth/service/AuthService.java) and [JwtFilter.java](file:///src/main/java/com/example/auth/security/JwtFilter.java).

### How it Works:
1. **User Login**: 
   - User inputs credentials at `/auth/login`.
   - Spring Boot validates password and creates a standard JWT.
   - We store the active token session in Redis:
     - **Key**: `session:token:<JWT-String>`
     - **Value**: `<username>`
     - **Expiry**: 24 hours (matching the JWT's lifespan).
2. **Request Validation**:
   - For every request, `JwtFilter` extracts the JWT from the `Authorization` header.
   - It queries Redis: `redisTemplate.opsForValue().get("session:token:<JWT>")`.
   - If the key does not exist, the session is invalid (expired or logged out), and the filter rejects the request with `401 Unauthorized`.
3. **User Logout**:
   - The user calls `/auth/logout` with their JWT.
   - The server deletes `session:token:<JWT-String>` from Redis.
   - Future requests using that token are rejected instantly because the key is missing from the whitelist, fulfilling secure session cleanup.

---

## Fail-Secure Resilience (No Silent Fallbacks)

Following robust security practices, **if Redis is offline, the API refuses connections**:
- If `StringRedisTemplate` throws a connection exception during rate checking or session lookup, the filter catches the exception and returns a visible `500 Internal Server Error`.
- We do **not** fall back to bypassing the rate limiter or session checks. This prevents users from circumventing security policies if the caching cluster undergoes maintenance.
