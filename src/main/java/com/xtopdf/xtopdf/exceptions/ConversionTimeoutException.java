package com.xtopdf.xtopdf.exceptions;

/**
 * Thrown when a file conversion operation exceeds the configured timeout.
 */
public class ConversionTimeoutException extends RuntimeException {

    public ConversionTimeoutException(String message) {
        super(message);
    }

    public ConversionTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
