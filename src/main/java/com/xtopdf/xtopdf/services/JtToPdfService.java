package com.xtopdf.xtopdf.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Service to convert JT Open files (JT) to PDF.
 */
@AllArgsConstructor
@Service
public class JtToPdfService {
    private final ProprietaryCadToPdfService proprietaryCadService;
    
    public void convertJtToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        String[] suggestions = {"Use JT2Go viewer to export", "Use CAD conversion tools that support JT format"};
        proprietaryCadService.convertToPdf(inputFile, pdfFile, "JT Open", "JT", suggestions);
    }
}
