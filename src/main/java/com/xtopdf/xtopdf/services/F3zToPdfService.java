package com.xtopdf.xtopdf.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Service to convert Fusion 360 Archive files (F3Z) to PDF.
 */
@AllArgsConstructor
@Service
public class F3zToPdfService {
    private final ProprietaryCadToPdfService proprietaryCadService;
    
    public void convertF3zToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        String[] suggestions = {"Extract and open in Fusion 360", " then export", "Use Fusion 360 cloud platform"};
        proprietaryCadService.convertToPdf(inputFile, pdfFile, "Fusion 360 Archive", "F3Z", suggestions);
    }
}
