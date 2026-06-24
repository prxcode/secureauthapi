# Use official eclipse-temurin Java 21 base image (alpine version for small footprint)
FROM eclipse-temurin:21-jre-alpine

# Copy the maven build jar into container as app.jar
COPY target/secureauthapi.jar app.jar

# Run the spring-boot jar
ENTRYPOINT ["java", "-jar", "/app.jar"]