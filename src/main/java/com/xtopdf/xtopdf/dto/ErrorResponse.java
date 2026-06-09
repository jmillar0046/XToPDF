package com.xtopdf.xtopdf.dto;

/**
 * Immutable record for error responses.
 * Provides structured error information to API clients.
 *
 * @param errorCode     Error code identifying the type of error (e.g., "CONVERSION_ERROR", "IO_ERROR", "INTERNAL_ERROR")
 * @param message       Human-readable error message describing what went wrong
 * @param correlationId Unique correlation ID for tracking this error across logs and systems
 */
public record ErrorResponse(String errorCode, String message, String correlationId) {}
