package com.xtopdf.xtopdf.controllers;

import com.xtopdf.xtopdf.dto.ErrorResponse;
import com.xtopdf.xtopdf.exceptions.FileConversionException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleRuntimeException() {
        RuntimeException exception = new RuntimeException("Test runtime exception");

        ResponseEntity<ErrorResponse> response = handler.handleRuntimeException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getBody().message()).isEqualTo("An unexpected error occurred");
        assertThat(response.getBody().message()).doesNotContain("Test runtime exception");
        assertThat(response.getBody().correlationId()).isNotNull();
        assertThat(response.getBody().correlationId()).isNotBlank();
    }

    @Test
    void testHandleFileConversionException() {
        FileConversionException exception = new FileConversionException("Test conversion error");

        ResponseEntity<ErrorResponse> response = handler.handleFileConversionException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("CONVERSION_ERROR");
        assertThat(response.getBody().message()).isEqualTo("File conversion failed");
        assertThat(response.getBody().message()).doesNotContain("Test conversion error");
        assertThat(response.getBody().correlationId()).isNotNull();
        assertThat(response.getBody().correlationId()).isNotBlank();
    }

    @Test
    void testHandleIOException() {
        IOException exception = new IOException("Test IO error");

        ResponseEntity<ErrorResponse> response = handler.handleIOException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("IO_ERROR");
        assertThat(response.getBody().message()).isEqualTo("A file operation error occurred");
        assertThat(response.getBody().message()).doesNotContain("Test IO error");
        assertThat(response.getBody().correlationId()).isNotNull();
        assertThat(response.getBody().correlationId()).isNotBlank();
    }

    @Test
    void testHandleMaxUploadSizeExceededException() {
        MaxUploadSizeExceededException exception = new MaxUploadSizeExceededException(1024 * 1024);

        ResponseEntity<ErrorResponse> response = handler.handleMaxUploadSizeExceededException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("PAYLOAD_TOO_LARGE");
        assertThat(response.getBody().message()).isEqualTo("File size exceeds the maximum allowed limit");
        assertThat(response.getBody().correlationId()).isNotNull();
        assertThat(response.getBody().correlationId()).isNotBlank();
    }

    @Test
    void testHandleMethodArgumentNotValidException() throws NoSuchMethodException {
        var bindingResult = new BeanPropertyBindingResult(new Object(), "testObject");
        bindingResult.addError(new FieldError("testObject", "fieldName", "must not be blank"));
        var methodParameter = new MethodParameter(
            GlobalExceptionHandlerTest.class.getDeclaredMethod("testHandleMethodArgumentNotValidException"), -1);
        var exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentNotValidException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().message()).contains("fieldName");
        assertThat(response.getBody().message()).contains("must not be blank");
        assertThat(response.getBody().correlationId()).isNotNull();
        assertThat(response.getBody().correlationId()).isNotBlank();
    }

    @Test
    void testHandleMethodArgumentNotValidException_multipleFields() throws NoSuchMethodException {
        var bindingResult = new BeanPropertyBindingResult(new Object(), "testObject");
        bindingResult.addError(new FieldError("testObject", "name", "must not be blank"));
        bindingResult.addError(new FieldError("testObject", "size", "must be positive"));
        var methodParameter = new MethodParameter(
            GlobalExceptionHandlerTest.class.getDeclaredMethod("testHandleMethodArgumentNotValidException_multipleFields"), -1);
        var exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentNotValidException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).contains("name: must not be blank");
        assertThat(response.getBody().message()).contains("size: must be positive");
    }
}
