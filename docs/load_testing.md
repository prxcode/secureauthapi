# Load Testing & Rate Limit Simulation

To verify that the JWT security architecture and Redis rate limiting can withstand production workloads, SecureAuthAPI provides a pre-configured python simulator.

---

## Asynchronous Load Test Simulator (`load_test.py`)

The simulator is located in [load_test.py](file:///load_test.py). It uses Python's standard library to dispatch concurrent HTTP request cycles to the authentication and user profile endpoints.

### Key Implementation Details:
- **Zero External Dependencies**: Implemented using python's built-in `urllib.request` and `concurrent.futures.ThreadPoolExecutor` (no pip package installs required).
- **Concurrency Model**: Spawns multiple concurrent threads simulating parallel user sessions.
- **Workflow Simulation**:
  1. Each simulated thread sends a `POST` request to `/auth/login` containing valid user credentials.
  2. The thread parses the resulting JSON and extracts the JWT.
  3. The thread uses the JWT to send a authenticated `GET` request to `/api/user/me`.
  4. Collects and classifies response codes (200 OK, 401 Unauthorized, 429 Rate Limited, 500 Server Error).

---

## Running the Load Test

1. Start the application server locally or inside Docker Compose.
2. Run the load test:
   ```bash
   python load_test.py
   ```

### Output Expectations (10 req/min limit)
The script fires **30 requests** concurrently. Because the rate limiter restricts each IP address to **10 requests per minute**, you will observe 10 successful cycles followed by 20 rate-limit blocks:

```text
==================================================
STARTING LOAD SIMULATION ON SECUREAUTHAPI
Total simulated request cycles: 30
Concurrency level: 5 parallel threads
==================================================
[User 1] Login Succeeded! Received JWT token.
[User 2] Login Succeeded! Received JWT token.
...
[User 11] Login Failed with HTTP Status: 429
[User 12] Login Failed with HTTP Status: 429
...

==================================================
SIMULATION RESULTS
==================================================
Total time elapsed: 0.82 seconds
HTTP 200 OK (Success):       10
HTTP 401 Unauthorized:       0
HTTP 403 Forbidden:          0
HTTP 429 Rate Limited:       20
HTTP 500 Server Error:       0
Network Connection Failures: 0
==================================================
```

This verifies that:
- The **Redis Rate Limiter** successfully thwarts rapid requests.
- The **JWT authentication** successfully handles valid tokens.
- **JSON-based error handling** correctly returns standard bodies under load.
- **Thread safety** is maintained in both the filter concurrency maps and Redis.
