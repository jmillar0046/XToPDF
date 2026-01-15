package com.xtopdf.xtopdf.controllers;

import com.xtopdf.xtopdf.dto.ErrorResponse;
import com.xtopdf.xtopdf.exceptions.FileConversionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.UUID;

/**
 * Global exception handler for the XToPDF application.
 * Provides consistent error responses with correlation IDs for tracking.
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
            ex.getMessage(),
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
            "File operation failed: " + ex.getMessage(),
            correlationId
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
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
            "An unexpected error occurred. Please contact support with correlation ID: " + correlationId,
            correlationId
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

