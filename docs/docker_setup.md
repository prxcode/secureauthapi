# Docker & Container Orchestration Setup

SecureAuthAPI is fully containerized using Docker and Docker Compose to ensure portable, consistent environments across local development and staging environments.

---

## The Dockerfile

The service uses a standard multi-stage/single-stage Docker execution environment tailored for Java 21:

```dockerfile
# Use official eclipse-temurin Java 21 base image (alpine version for small footprint)
FROM eclipse-temurin:21-jre-alpine

# Copy the maven build jar into container as app.jar
COPY target/secureauthapi.jar app.jar

# Run the spring-boot jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

- **Base Image**: `eclipse-temurin:21-jre-alpine` (highly lightweight Alpine Linux JRE distribution, optimizing container size to ~100MB).
- **Target Jar**: Uses the build-configured `<finalName>` output `target/secureauthapi.jar`.

---

## Multi-Container Orchestration (`docker-compose`)

To run the application alongside its Redis dependency without requiring manual installation, we use [docker-compose.yml](file:///docker-compose.yml).

```yaml
version: '3.8'

services:
  # Redis cache database container
  redis:
    image: redis:alpine
    container_name: secureauth-redis
    ports:
      - "6379:6379"
    restart: always

  # Spring Boot REST API container
  app:
    build: .
    container_name: secureauth-app
    ports:
      - "8080:8080"
    depends_on:
      - redis
    environment:
      # Inject redis host matching the service name above
      - SPRING_DATA_REDIS_HOST=redis
      - SPRING_DATA_REDIS_PORT=6379
      
      # Disable IP filtering inside container by default,
      # preventing docker internal routing from causing 403 Forbidden on host access
      - SECURITY_IP_FILTER_ENABLED=false
    restart: always
```

### Key Configurations:
1. **Service DNS Name Linking**: The Spring Boot container (`app`) connects to Redis using the hostname `redis` (resolving automatically via Docker's built-in DNS network).
2. **Environment Variable Injection**: Environment settings in Spring Boot are overridden inside compose:
   - `SPRING_DATA_REDIS_HOST=redis` points to the Redis container.
   - `SECURITY_IP_FILTER_ENABLED=false` disables the IP address restriction filter. Inside Docker networks, host requests are routed through a virtual bridge network (e.g., `172.18.0.1`), which would normally trigger the localhost-only filter (`127.0.0.1`) and throw `403 Forbidden`. Disabling the filter ensures seamless testing from the host browser.

---

## Run Container Build Command
Ensure Docker Desktop is running, then launch the containers:

```bash
docker-compose up --build
```

To stop and remove containers:
```bash
docker-compose down
```
