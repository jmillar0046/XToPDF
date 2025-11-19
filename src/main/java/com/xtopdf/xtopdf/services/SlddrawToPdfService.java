package com.xtopdf.xtopdf.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Service to convert SolidWorks Drawing files (SLDDRW) to PDF.
 */
@AllArgsConstructor
@Service
public class SlddrawToPdfService {
    private final ProprietaryCadToPdfService proprietaryCadService;
    
    public void convertSlddrawToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        String[] suggestions = {"Use SolidWorks to export to PDF directly", "Use eDrawings Viewer to convert"};
        proprietaryCadService.convertToPdf(inputFile, pdfFile, "SolidWorks Drawing", "SLDDRW", suggestions);
    }
}
