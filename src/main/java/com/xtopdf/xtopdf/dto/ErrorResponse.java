package com.xtopdf.xtopdf.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for error responses.
 * Provides structured error information to API clients.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    
    /**
     * Error code identifying the type of error.
     * Examples: "CONVERSION_ERROR", "IO_ERROR", "INTERNAL_ERROR"
     */
    private String errorCode;
    
    /**
     * Human-readable error message describing what went wrong.
     */
    private String message;
    
    /**
     * Unique correlation ID for tracking this error across logs and systems.
     * Generated using UUID.randomUUID().toString()
     */
    private String correlationId;
}
