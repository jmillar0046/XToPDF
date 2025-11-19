package com.xtopdf.xtopdf.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Service to convert CATIA Drawing files (CATDRAWING) to PDF.
 */
@AllArgsConstructor
@Service
public class CatdrawingToPdfService {
    private final ProprietaryCadToPdfService proprietaryCadService;
    
    public void convertCatdrawingToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        String[] suggestions = {"Use CATIA to export to PDF directly", "Use CADExchanger or other conversion tools"};
        proprietaryCadService.convertToPdf(inputFile, pdfFile, "CATIA Drawing", "CATDRAWING", suggestions);
    }
}
