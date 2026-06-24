# JWT Authentication & Role-Based Access Control (RBAC)

SecureAuthAPI provides stateless authentication by issuing JSON Web Tokens (JWT) upon successful login. Authorization levels are checked at the controller gateway using Spring Security role restrictions.

---

## JWT Token Specifications

Tokens are generated and validated in [JwtUtil.java](file:///src/main/java/com/example/auth/security/JwtUtil.java).

- **Signature Algorithm**: HMAC SHA-256 (`HS256`).
- **Signing Key**: Generated using `Keys.hmacShaKeyFor` with a secure 256-bit secret string key.
- **Expiration Time**: `86400000 ms` (24 hours).
- **Subject**: Contains the authenticated username.

### Generation Flow
When a client logs in via `/auth/login`, credentials are verified by the `AuthenticationManager`. If successful, the subject is signed:
```java
return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 86400000))
        .signWith(KEY, SignatureAlgorithm.HS256)
        .compact();
```

---

## Role-Based Authorization Mapping

RBAC rules are managed centrally in [SecurityConfig.java](file:///src/main/java/com/example/auth/security/SecurityConfig.java):

```java
.authorizeHttpRequests(auth -> auth
    // Public paths
    .requestMatchers("/", "/auth/login", "/auth/logout", "/error", "/css/**", "/js/**").permitAll()
    // Admin restricted
    .requestMatchers("/api/admin").hasRole("ADMIN")
    // User profile (Any authenticated role)
    .requestMatchers("/api/user/me").authenticated()
    .anyRequest().authenticated()
)
```

### Initial Users
During startup, [SecureAuthApiApplication.java](file:///src/main/java/com/example/auth/SecureAuthApiApplication.java) provisions two default accounts using `BCryptPasswordEncoder` to hash user passwords:

1. **`testuser`**
   - **Role**: `ROLE_USER`
   - **Default Password**: `password123`
   - **Access**: Can access `/api/user/me` but receives `403 Forbidden` on `/api/admin`.
2. **`admin`**
   - **Role**: `ROLE_ADMIN`
   - **Default Password**: `admin123`
   - **Access**: Can access both `/api/user/me` and `/api/admin`.

---

## Security Best Practices Implemented
- **BCrypt Password Hashing**: Passwords are never stored in plain-text. They are hashed using a strong work-factor BCrypt encoder.
- **Stateless HTTP Session Management**: Disabled cookie-based sessions (`SessionCreationPolicy.STATELESS`) to mitigate **Cross-Site Request Forgery (CSRF)** attacks.
- **Custom JSON Failures**: Any authentication exception returns standard JSON error bodies instead of redirecting users to HTML pages, keeping the REST design consistent.
