package com.xtopdf.xtopdf.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Service to convert 3D Manufacturing Format files (3MF) to PDF.
 */
@AllArgsConstructor
@Service
public class ThreeMfToPdfService {
    private final ProprietaryCadToPdfService proprietaryCadService;
    
    public void convert3mfToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        String[] suggestions = {"Use 3D Builder or other 3MF viewers", "Export to STL for better compatibility"};
        proprietaryCadService.convertToPdf(inputFile, pdfFile, "3D Manufacturing Format", "3MF", suggestions);
    }
}
