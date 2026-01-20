package com.xtopdf.xtopdf.controllers;

import com.xtopdf.xtopdf.dto.ErrorResponse;
import com.xtopdf.xtopdf.exceptions.FileConversionException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    @Test
    void testHandleRuntimeException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        RuntimeException exception = new RuntimeException("Test runtime exception");

        ResponseEntity<ErrorResponse> response = handler.handleRuntimeException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_ERROR", response.getBody().getErrorCode());
        assertNotNull(response.getBody().getCorrelationId());
        assertTrue(response.getBody().getMessage().contains("unexpected error"));
    }
    
    @Test
    void testHandleFileConversionException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        FileConversionException exception = new FileConversionException("Test conversion error");

        ResponseEntity<ErrorResponse> response = handler.handleFileConversionException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("CONVERSION_ERROR", response.getBody().getErrorCode());
        assertEquals("Test conversion error", response.getBody().getMessage());
        assertNotNull(response.getBody().getCorrelationId());
    }
    
    @Test
    void testHandleIOException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        IOException exception = new IOException("Test IO error");

        ResponseEntity<ErrorResponse> response = handler.handleIOException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("IO_ERROR", response.getBody().getErrorCode());
        assertTrue(response.getBody().getMessage().contains("File operation failed"));
        assertNotNull(response.getBody().getCorrelationId());
    }
}
