package com.xtopdf.xtopdf.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Service to convert SolidWorks Part files (SLDPRT) to PDF.
 */
@AllArgsConstructor
@Service
public class SldprtToPdfService {
    private final ProprietaryCadToPdfService proprietaryCadService;
    
    public void convertSldprtToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        String[] suggestions = {"Use SolidWorks to export to PDF or STEP", "Use SolidWorks API or eDrawings for conversion"};
        proprietaryCadService.convertToPdf(inputFile, pdfFile, "SolidWorks Part", "SLDPRT", suggestions);
    }
}
