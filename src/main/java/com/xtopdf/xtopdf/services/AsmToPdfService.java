package com.xtopdf.xtopdf.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Service to convert Creo Assembly files (ASM) to PDF.
 */
@AllArgsConstructor
@Service
public class AsmToPdfService {
    private final ProprietaryCadToPdfService proprietaryCadService;
    
    public void convertAsmToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        String[] suggestions = {"Use Creo to export to STEP or PDF", "Use Creo View for visualization"};
        proprietaryCadService.convertToPdf(inputFile, pdfFile, "Creo Assembly", "ASM", suggestions);
    }
}
