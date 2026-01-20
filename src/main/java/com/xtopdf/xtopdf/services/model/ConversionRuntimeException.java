package com.xtopdf.xtopdf.services.model;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

/**
 * Runtime exception wrapper for FileConversionException.
 * This is necessary because the Runnable interface doesn't allow checked exceptions.
 */
public class ConversionRuntimeException extends RuntimeException {
    public final FileConversionException fileConversionException;
    
    public ConversionRuntimeException(FileConversionException fileConversionException) {
        super(fileConversionException);
        this.fileConversionException = fileConversionException;
    }
    
    public FileConversionException getFileConversionException() {
        return fileConversionException;
    }
}
