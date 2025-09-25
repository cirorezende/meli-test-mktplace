# Multi-stage Dockerfile for Orders Processing System
# Stage 1: Build stage
FROM maven:3.9.6 AS builder

LABEL maintainer="Orders Processing System"
LABEL stage="builder"

# Set working directory
WORKDIR /app

# Copy Maven descriptor
COPY pom.xml ./

# Pre-fetch dependencies for caching
RUN mvn -q dependency:go-offline

# Copy sources
COPY src src

# Build application jar (skip tests for image build speed)
RUN mvn -q clean package -DskipTests

# Verify JAR was created
RUN ls -la target/ && test -f target/*.jar

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre

LABEL maintainer="Orders Processing System"
LABEL stage="runtime"
LABEL version="1.0.0"

# Install required packages for observability and debugging
RUN apk add --no-cache \
    curl \
    jq \
    tzdata \
    && rm -rf /var/cache/apk/*

# Set timezone
ENV TZ=America/Sao_Paulo

# Create non-root user for security
RUN addgroup -g 1001 appgroup && \
    adduser -u 1001 -G appgroup -s /bin/sh -D appuser

# Create app directory
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Create logs directory and set permissions
RUN mkdir -p /app/logs && \
    chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# JVM configuration for containers
ENV JAVA_OPTS="-server \
    -Xms512m \
    -Xmx1024m \
    -XX:+UseG1GC \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=80 \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/app/logs/ \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.backgroundpreinitializer.ignore=true"

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Application configuration
ENV SPRING_PROFILES_ACTIVE=docker

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]