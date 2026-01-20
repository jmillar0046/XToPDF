package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

/**
 * Runtime exception wrapper for FileConversionException.
 * This is necessary because the Runnable interface doesn't allow checked exceptions.
 */
class ConversionRuntimeException extends RuntimeException {
    private final FileConversionException fileConversionException;
    
    public ConversionRuntimeException(FileConversionException fileConversionException) {
        super(fileConversionException);
        this.fileConversionException = fileConversionException;
    }
    
    public FileConversionException getFileConversionException() {
        return fileConversionException;
    }
}
