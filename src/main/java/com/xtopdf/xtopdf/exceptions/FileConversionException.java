package com.xtopdf.xtopdf.exceptions;

public class FileConversionException extends Exception {

    // Default constructor
    public FileConversionException() {
        super("File conversion failed");
    }

    // Constructor that accepts a custom message
    public FileConversionException(String message) {
        super(message);
    }

    // Constructor that accepts a cause
    public FileConversionException(Throwable cause) {
        super(cause);
    }

    // Constructor that accepts both a custom message and a cause
    public FileConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}