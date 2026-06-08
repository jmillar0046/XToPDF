package com.xtopdf.xtopdf.controllers;

import com.xtopdf.xtopdf.dto.ErrorResponse;
import com.xtopdf.xtopdf.exceptions.ConversionTimeoutException;
import com.xtopdf.xtopdf.exceptions.FileConversionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * Global exception handler for the XToPDF application.
 * Provides consistent error responses with correlation IDs for tracking.
 * All handlers return generic safe messages — never exposing internal details.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle file conversion exceptions.
     * These are expected errors during file conversion operations.
     */
    @ExceptionHandler(FileConversionException.class)
    public ResponseEntity<ErrorResponse> handleFileConversionException(FileConversionException ex) {
        String correlationId = UUID.randomUUID().toString();
        log.error("File conversion error [correlationId={}]: {}", correlationId, ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse(
            "CONVERSION_ERROR",
            "File conversion failed",
            correlationId
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle I/O exceptions.
     * These occur during file read/write operations.
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleIOException(IOException ex) {
        String correlationId = UUID.randomUUID().toString();
        log.error("I/O error [correlationId={}]: {}", correlationId, ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse(
            "IO_ERROR",
            "A file operation error occurred",
            correlationId
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Handle illegal argument exceptions.
     * These are client errors due to invalid request parameters.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        String correlationId = UUID.randomUUID().toString();
        log.error("Invalid argument [correlationId={}]: {}", correlationId, ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse(
            "INVALID_ARGUMENT",
            "Invalid request parameters",
            correlationId
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle unexpected runtime exceptions.
     * These are unexpected errors that should be investigated.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        String correlationId = UUID.randomUUID().toString();
        log.error("Unexpected error [correlationId={}]: {}", correlationId, ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse(
            "INTERNAL_ERROR",
            "An unexpected error occurred",
            correlationId
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Handle file upload size exceeded exceptions.
     * Triggered when uploaded file exceeds configured limits.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        String correlationId = UUID.randomUUID().toString();
        log.error("File upload size exceeded [correlationId={}]: {}", correlationId, ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse(
            "PAYLOAD_TOO_LARGE",
            "File size exceeds the maximum allowed limit",
            correlationId
        );
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(error);
    }

    /**
     * Handle bean validation exceptions.
     * Returns field-level validation error messages.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String correlationId = UUID.randomUUID().toString();
        log.error("Validation error [correlationId={}]: {}", correlationId, ex.getMessage(), ex);

        String fieldErrors = ex.getBindingResult().getFieldErrors().stream()
            .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
            .collect(Collectors.joining("; "));

        ErrorResponse error = new ErrorResponse(
            "VALIDATION_ERROR",
            fieldErrors,
            correlationId
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle conversion timeout exceptions.
     * Triggered when a conversion operation exceeds the configured time limit.
     */
    @ExceptionHandler(ConversionTimeoutException.class)
    public ResponseEntity<ErrorResponse> handleConversionTimeoutException(ConversionTimeoutException ex) {
        String correlationId = UUID.randomUUID().toString();
        log.error("Conversion timeout [correlationId={}]: {}", correlationId, ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse(
            "CONVERSION_TIMEOUT",
            "Conversion timed out",
            correlationId
        );
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(error);
    }
}
