package com.xtopdf.xtopdf.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Service to convert SolidWorks Assembly files (SLDASM) to PDF.
 */
@AllArgsConstructor
@Service
public class SldasmToPdfService {
    private final ProprietaryCadToPdfService proprietaryCadService;
    
    public void convertSldasmToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        String[] suggestions = {"Use SolidWorks to export to PDF or STEP", "Use SolidWorks API or eDrawings for conversion"};
        proprietaryCadService.convertToPdf(inputFile, pdfFile, "SolidWorks Assembly", "SLDASM", suggestions);
    }
}
