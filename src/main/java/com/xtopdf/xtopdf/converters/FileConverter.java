package com.xtopdf.xtopdf.converters;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface FileConverter {
    void convertToPDF(MultipartFile inputFile, String outputFile);
}
