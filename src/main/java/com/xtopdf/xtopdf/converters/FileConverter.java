package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface FileConverter {
    void convertToPDF(MultipartFile inputFile, String outputFile) throws FileConversionException;
    
    default void convertToPDF(MultipartFile inputFile, String outputFile, boolean executeMacros) throws FileConversionException {
        // Default implementation delegates to original method for backward compatibility
        convertToPDF(inputFile, outputFile);
    }
}
