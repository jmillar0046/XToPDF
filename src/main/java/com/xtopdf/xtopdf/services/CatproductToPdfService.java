package com.xtopdf.xtopdf.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Service to convert CATIA Product files (CATPRODUCT) to PDF.
 */
@AllArgsConstructor
@Service
public class CatproductToPdfService {
    private final ProprietaryCadToPdfService proprietaryCadService;
    
    public void convertCatproductToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        String[] suggestions = {"Use CATIA to export to STEP or PDF", "Use CADExchanger or other conversion tools"};
        proprietaryCadService.convertToPdf(inputFile, pdfFile, "CATIA Product", "CATPRODUCT", suggestions);
    }
}
