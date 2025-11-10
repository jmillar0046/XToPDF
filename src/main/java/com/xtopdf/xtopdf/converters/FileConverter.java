package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.config.PageNumberConfig;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface FileConverter {
    void convertToPDF(MultipartFile inputFile, String outputFile);
    
    default void convertToPDF(MultipartFile inputFile, String outputFile, PageNumberConfig pageNumberConfig) {
        convertToPDF(inputFile, outputFile);
    }
}
