# Multi-stage build for XToPDF conversion service
FROM eclipse-temurin:25-jdk-alpine AS build

# Set working directory
WORKDIR /app

# Copy gradle wrapper and config first for better caching
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x ./gradlew

# Download dependencies (cached if dependencies don't change)
RUN ./gradlew dependencies --no-daemon || true

# Copy source code
COPY src ./src

# Build the application
RUN ./gradlew bootJar --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:25-jre-alpine

# Install required packages for PDF conversion
RUN apk add --no-cache \
    fontconfig \
    ttf-dejavu

# Create app user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Create directories for input/output
RUN mkdir -p /app/input /app/output && \
    chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Run the application with preview features enabled
ENTRYPOINT ["java", "--enable-preview", "-jar", "app.jar"]
