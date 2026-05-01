package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

public interface FileConverter {
    void convertToPDF(MultipartFile inputFile, String outputFile) throws FileConversionException;
    
    default void convertToPDF(MultipartFile inputFile, String outputFile, boolean executeMacros) throws FileConversionException {
        // Default implementation delegates to original method for backward compatibility
        convertToPDF(inputFile, outputFile);
    }

    /**
     * Returns the file extensions this converter supports, including the leading dot.
     * Example: Set.of(".csv", ".tsv")
     */
    Set<String> getSupportedExtensions();
}
