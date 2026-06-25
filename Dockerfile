# Stage 1: Build the application using Maven
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src
# Build the application (skipping tests for faster deployment)
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copy the built jar from the build stage
COPY --from=build /app/target/secureauthapi*.jar app.jar

# Run the spring-boot jar, forcing the correct JDBC URL via command line to override any phantom Render environment variables
ENTRYPOINT ["sh", "-c", "java -jar app.jar --spring.datasource.url=jdbc:postgresql://${DB_HOST:-localhost}:${DB_PORT:-5433}/${DB_NAME:-authdb}"]