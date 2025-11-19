package com.xtopdf.xtopdf.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Service to convert Revit files (RVT) to PDF.
 */
@AllArgsConstructor
@Service
public class RvtToPdfService {
    private final ProprietaryCadToPdfService proprietaryCadService;
    
    public void convertRvtToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        String[] suggestions = {"Use Revit to export to PDF or DWG", "Use Revit Viewer for visualization"};
        proprietaryCadService.convertToPdf(inputFile, pdfFile, "Revit", "RVT", suggestions);
    }
}
