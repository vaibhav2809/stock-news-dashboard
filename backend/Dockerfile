# Stage 1: Build the application
FROM gradle:8-jdk21 AS builder

WORKDIR /app

# Copy Gradle wrapper and build files first for better layer caching
COPY gradle/ gradle/
COPY gradlew build.gradle settings.gradle ./

# Fix execute permission (Windows Git strips it) and override gradle.properties for Linux
RUN chmod +x gradlew && \
    echo "org.gradle.jvmargs=-Djava.net.preferIPv4Stack=true" > gradle.properties && \
    echo "org.gradle.daemon=false" >> gradle.properties

# Download dependencies (cached unless build files change)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src/ src/

# Build the fat JAR
RUN ./gradlew bootJar --no-daemon

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the built JAR from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Switch to non-root user
USER appuser

# Railway injects PORT; default to 8080 for docker-compose
ENV PORT=8080

EXPOSE ${PORT}

HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=40s \
  CMD wget --spider -q http://localhost:${PORT}/api/v1/news/trending?size=1 || exit 1

# Use PORT env var so Railway can assign its own port
ENTRYPOINT ["sh", "-c", "java -jar app.jar --spring.profiles.active=prod --server.port=${PORT}"]
