package com.xtopdf.xtopdf.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Service to convert CATIA Part files (CATPART) to PDF.
 */
@AllArgsConstructor
@Service
public class CatpartToPdfService {
    private final ProprietaryCadToPdfService proprietaryCadService;
    
    public void convertCatpartToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        String[] suggestions = {"Use CATIA to export to STEP or PDF", "Use CADExchanger or other conversion tools"};
        proprietaryCadService.convertToPdf(inputFile, pdfFile, "CATIA Part", "CATPART", suggestions);
    }
}
