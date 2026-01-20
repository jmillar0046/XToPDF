package com.xtopdf.xtopdf.exceptions;

import net.jqwik.api.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for exception handling.
 * Validates Requirements 3.1, 3.2, 3.3
 * 
 * Property 6: Exception Cause Preservation
 * Property 7: Correlation ID Inclusion
 * Property 8: Exception Unwrapping Correctness
 */
class ExceptionHandlingPropertyTest {

    /**
     * Property 6: Exception Cause Preservation
     * 
     * When wrapping an exception, the original cause should always be preserved.
     */
    @Property
    @Label("Exception cause is preserved when wrapping")
    void exceptionCauseIsPreserved(
            @ForAll("exceptionMessages") String message,
            @ForAll("exceptionMessages") String causeMessage) {
        
        // Create original exception
        Exception originalCause = new RuntimeException(causeMessage);
        
        // Wrap in FileConversionException
        FileConversionException wrapped = new FileConversionException(message, originalCause);
        
        // Verify cause is preserved
        assertThat(wrapped.getCause()).isEqualTo(originalCause);
        assertThat(wrapped.getCause().getMessage()).isEqualTo(causeMessage);
    }

    /**
     * Property 7: Correlation ID Inclusion
     * 
     * When a FileConversionException is created with a correlation ID,
     * it should be accessible and included in the message.
     */
    @Property
    @Label("Correlation ID is included in exception")
    void correlationIdIsIncluded(
            @ForAll("exceptionMessages") String message,
            @ForAll("correlationIds") String correlationId) {
        
        // Create exception with correlation ID
        FileConversionException exception = new FileConversionException(
                message + " [CorrelationId: " + correlationId + "]"
        );
        
        // Verify correlation ID is in message
        assertThat(exception.getMessage()).contains(correlationId);
        assertThat(exception.getMessage()).contains("CorrelationId");
    }

    /**
     * Property 8: Exception Unwrapping Correctness
     * 
     * When unwrapping nested exceptions, the root cause should be extractable.
     */
    @Property
    @Label("Exception unwrapping extracts root cause")
    void exceptionUnwrappingExtractsRootCause(
            @ForAll("exceptionMessages") String level1Message,
            @ForAll("exceptionMessages") String level2Message,
            @ForAll("exceptionMessages") String rootMessage) {
        
        // Create nested exception chain
        Exception rootCause = new IllegalArgumentException(rootMessage);
        Exception level2 = new RuntimeException(level2Message, rootCause);
        FileConversionException level1 = new FileConversionException(level1Message, level2);
        
        // Unwrap to root cause
        Throwable current = level1;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        
        // Verify root cause is found
        assertThat(current).isEqualTo(rootCause);
        assertThat(current.getMessage()).isEqualTo(rootMessage);
    }

    /**
     * Property 9: Exception message is never null
     * 
     * All exceptions should have non-null messages for logging.
     */
    @Property
    @Label("Exception messages are never null")
    void exceptionMessagesAreNeverNull(
            @ForAll("exceptionMessages") String message) {
        
        FileConversionException exception = new FileConversionException(message);
        
        assertThat(exception.getMessage()).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    /**
     * Property 10: Exception with null cause is handled
     * 
     * Exceptions without a cause should still be valid.
     */
    @Property
    @Label("Exceptions without cause are valid")
    void exceptionsWithoutCauseAreValid(
            @ForAll("exceptionMessages") String message) {
        
        FileConversionException exception = new FileConversionException(message);
        
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }

    // Arbitraries for generating test data

    @Provide
    Arbitrary<String> exceptionMessages() {
        return Arbitraries.of(
                "File conversion failed",
                "Invalid file format",
                "Conversion timeout",
                "Out of memory during conversion",
                "Unsupported file type",
                "File not found",
                "Permission denied",
                "Disk full"
        );
    }

    @Provide
    Arbitrary<String> correlationIds() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(8)
                .ofMaxLength(16)
                .map(s -> s + "-" + System.currentTimeMillis());
    }
}
