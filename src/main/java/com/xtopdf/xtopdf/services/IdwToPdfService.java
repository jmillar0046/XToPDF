package com.xtopdf.xtopdf.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Service to convert Inventor Drawing files (IDW) to PDF.
 */
@AllArgsConstructor
@Service
public class IdwToPdfService {
    private final ProprietaryCadToPdfService proprietaryCadService;
    
    public void convertIdwToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        String[] suggestions = {"Use Autodesk Inventor to export to PDF directly", "Use Inventor View for visualization"};
        proprietaryCadService.convertToPdf(inputFile, pdfFile, "Inventor Drawing", "IDW", suggestions);
    }
}
