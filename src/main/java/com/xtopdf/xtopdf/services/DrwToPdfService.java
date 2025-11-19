package com.xtopdf.xtopdf.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Service to convert Creo Drawing files (DRW) to PDF.
 */
@AllArgsConstructor
@Service
public class DrwToPdfService {
    private final ProprietaryCadToPdfService proprietaryCadService;
    
    public void convertDrwToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        String[] suggestions = {"Use Creo to export to PDF directly", "Use Creo View for visualization"};
        proprietaryCadService.convertToPdf(inputFile, pdfFile, "Creo Drawing", "DRW", suggestions);
    }
}
