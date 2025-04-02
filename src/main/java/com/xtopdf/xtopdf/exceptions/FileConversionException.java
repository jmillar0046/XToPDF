package com.xtopdf.xtopdf.exceptions;

public class FileConversionException extends Exception {

    public FileConversionException() {
        super("File conversion failed");
    }

    public FileConversionException(String message) {
        super(message);
    }
}