# Stage 1: Build the application
FROM gradle:8-jdk21 AS builder

WORKDIR /app

# Copy Gradle wrapper and build files first for better layer caching
COPY gradle/ gradle/
COPY gradlew build.gradle settings.gradle ./

# Override gradle.properties for Linux (the local file has Windows-specific tmpdir path)
RUN echo "org.gradle.jvmargs=-Djava.net.preferIPv4Stack=true" > gradle.properties && \
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

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=40s \
  CMD wget --spider -q http://localhost:8080/api/v1/news/trending?size=1 || exit 1

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
