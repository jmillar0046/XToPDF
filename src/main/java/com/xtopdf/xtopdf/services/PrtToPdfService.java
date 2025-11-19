package com.xtopdf.xtopdf.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Service to convert Creo Part files (PRT) to PDF.
 */
@AllArgsConstructor
@Service
public class PrtToPdfService {
    private final ProprietaryCadToPdfService proprietaryCadService;
    
    public void convertPrtToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        String[] suggestions = {"Use Creo to export to STEP or PDF", "Use Creo View for visualization"};
        proprietaryCadService.convertToPdf(inputFile, pdfFile, "Creo Part", "PRT", suggestions);
    }
}
