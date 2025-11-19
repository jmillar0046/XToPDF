package com.xtopdf.xtopdf.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Service to convert MicroStation files (DGN) to PDF.
 */
@AllArgsConstructor
@Service
public class DgnToPdfService {
    private final ProprietaryCadToPdfService proprietaryCadService;
    
    public void convertDgnToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        String[] suggestions = {"Use MicroStation to export to PDF or DWG", "Use Bentley View for visualization"};
        proprietaryCadService.convertToPdf(inputFile, pdfFile, "MicroStation", "DGN", suggestions);
    }
}
