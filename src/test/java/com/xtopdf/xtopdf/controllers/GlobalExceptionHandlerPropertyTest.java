package com.xtopdf.xtopdf.controllers;

import com.xtopdf.xtopdf.dto.ErrorResponse;
import com.xtopdf.xtopdf.exceptions.FileConversionException;
import net.jqwik.api.*;
import net.jqwik.api.lifecycle.BeforeProperty;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for GlobalExceptionHandler error response sanitization.
 * 
 * Validates: Requirements 1.1, 1.2, 1.3
 * 
 * These tests verify that AFTER hardening, no exception message content leaks
 * into the HTTP response body. The handler should return only generic safe messages
 * with a correlation ID, never exposing internal details like file paths, class names,
 * connection strings, or stack traces.
 * 
 * NOTE: This is TDD — these tests will FAIL against the current implementation
 * (which exposes ex.getMessage()). Task 4.2 will fix the implementation.
 */
@Tag("Feature: codebase-hardening, Property 1: Error response sanitization")
class GlobalExceptionHandlerPropertyTest {

    private GlobalExceptionHandler handler;

    @BeforeProperty
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    /**
     * Property 1a: FileConversionException message sanitization
     * 
     * For any FileConversionException with sensitive message content, the HTTP response
     * body SHALL NOT contain the exception's getMessage() content. The response SHALL
     * contain a valid UUID correlationId and a generic safe message.
     */
    @Property(tries = 25)
    @Label("FileConversionException messages are not leaked in response")
    void fileConversionExceptionMessagesAreNotLeaked(
            @ForAll("sensitiveMessages") String sensitiveMessage) {

        FileConversionException exception = new FileConversionException(sensitiveMessage);

        ResponseEntity<ErrorResponse> response = handler.handleFileConversionException(exception);

        assertThat(response.getBody()).isNotNull();
        ErrorResponse body = response.getBody();

        // The response message SHALL NOT contain the exception's getMessage() content
        assertThat(body.message())
                .as("Response message must not contain sensitive exception message: %s", sensitiveMessage)
                .doesNotContain(sensitiveMessage);

        // The response SHALL contain a valid non-null correlationId (UUID format)
        assertThat(body.correlationId()).isNotNull();
        assertThat(body.correlationId()).isNotBlank();
        assertThat(UUID.fromString(body.correlationId()))
                .as("correlationId must be a valid UUID")
                .isNotNull();

        // The response SHALL have a generic safe message
        assertThat(body.message())
                .as("Response must contain a generic safe message")
                .isEqualTo("File conversion failed");
    }

    /**
     * Property 1b: IOException message sanitization
     * 
     * For any IOException with sensitive message content, the HTTP response body
     * SHALL NOT contain the exception's getMessage() content. The response SHALL
     * contain a valid UUID correlationId and a generic safe message.
     */
    @Property(tries = 25)
    @Label("IOException messages are not leaked in response")
    void ioExceptionMessagesAreNotLeaked(
            @ForAll("sensitiveMessages") String sensitiveMessage) {

        IOException exception = new IOException(sensitiveMessage);

        ResponseEntity<ErrorResponse> response = handler.handleIOException(exception);

        assertThat(response.getBody()).isNotNull();
        ErrorResponse body = response.getBody();

        // The response message SHALL NOT contain the exception's getMessage() content
        assertThat(body.message())
                .as("Response message must not contain sensitive exception message: %s", sensitiveMessage)
                .doesNotContain(sensitiveMessage);

        // The response SHALL contain a valid non-null correlationId (UUID format)
        assertThat(body.correlationId()).isNotNull();
        assertThat(body.correlationId()).isNotBlank();
        assertThat(UUID.fromString(body.correlationId()))
                .as("correlationId must be a valid UUID")
                .isNotNull();

        // The response SHALL have a generic safe message
        assertThat(body.message())
                .as("Response must contain a generic safe message")
                .isEqualTo("A file operation error occurred");
    }

    /**
     * Property 1c: RuntimeException message sanitization
     * 
     * For any RuntimeException with sensitive message content, the HTTP response body
     * SHALL NOT contain the exception's getMessage() content. The response SHALL
     * contain a valid UUID correlationId and a generic safe message.
     */
    @Property(tries = 25)
    @Label("RuntimeException messages are not leaked in response")
    void runtimeExceptionMessagesAreNotLeaked(
            @ForAll("sensitiveMessages") String sensitiveMessage) {

        RuntimeException exception = new RuntimeException(sensitiveMessage);

        ResponseEntity<ErrorResponse> response = handler.handleRuntimeException(exception);

        assertThat(response.getBody()).isNotNull();
        ErrorResponse body = response.getBody();

        // The response message SHALL NOT contain the exception's getMessage() content
        assertThat(body.message())
                .as("Response message must not contain sensitive exception message: %s", sensitiveMessage)
                .doesNotContain(sensitiveMessage);

        // The response SHALL contain a valid non-null correlationId (UUID format)
        assertThat(body.correlationId()).isNotNull();
        assertThat(body.correlationId()).isNotBlank();
        assertThat(UUID.fromString(body.correlationId()))
                .as("correlationId must be a valid UUID")
                .isNotNull();

        // The response SHALL have a generic safe message that does NOT include the exception message
        assertThat(body.message())
                .as("Response must contain a generic safe message")
                .isEqualTo("An unexpected error occurred");
    }

    // --- Arbitraries ---

    /**
     * Generates sensitive-looking strings that should NEVER appear in API responses.
     * Includes file paths, class names, connection strings, SQL queries, passwords, etc.
     */
    @Provide
    Arbitrary<String> sensitiveMessages() {
        return Arbitraries.of(
                // File paths
                "/usr/local/secret.pdf",
                "/home/admin/.ssh/id_rsa",
                "/etc/passwd",
                "/var/lib/xtopdf/temp/conversion_12345.pdf",
                "C:\\Users\\admin\\Documents\\secret.docx",
                "/opt/xtopdf/config/application-prod.yml",

                // Class names and stack trace fragments
                "com.xtopdf.internal.Service",
                "com.xtopdf.xtopdf.adapters.container.PodmanContainerAdapter.createAndStartContainer",
                "java.lang.NullPointerException: Cannot invoke method on null",
                "at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1089)",

                // Connection strings
                "jdbc:postgresql://host:5432/db",
                "jdbc:mysql://prod-db.internal:3306/xtopdf?user=admin&password=s3cret",
                "mongodb://admin:password123@mongo.internal:27017/xtopdf",
                "redis://default:p4ssw0rd@redis.internal:6379",

                // SQL queries
                "SELECT * FROM users WHERE id = 1; DROP TABLE users;",
                "INSERT INTO audit_log (action, user_id) VALUES ('convert', 42)",

                // Passwords and secrets
                "password=SuperSecret123!",
                "api_key=sk-proj-abc123def456ghi789",
                "AWS_SECRET_ACCESS_KEY=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY",
                "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkw",

                // Internal error details
                "Failed to allocate 2GB memory for PDF rendering",
                "Container podman-xtopdf-12345 exited with code 137 (OOMKilled)",
                "Socket timeout after 30000ms connecting to 10.0.0.5:8080",
                "Permission denied: /tmp/xtopdf/output_a1b2c3d4.pdf"
        );
    }
}
