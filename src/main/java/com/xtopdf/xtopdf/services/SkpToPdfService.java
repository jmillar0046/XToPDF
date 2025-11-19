package com.xtopdf.xtopdf.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Service to convert SketchUp files (SKP) to PDF.
 */
@AllArgsConstructor
@Service
public class SkpToPdfService {
    private final ProprietaryCadToPdfService proprietaryCadService;
    
    public void convertSkpToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        String[] suggestions = {"Use SketchUp to export to PDF or DWG", "Use SketchUp Viewer for visualization"};
        proprietaryCadService.convertToPdf(inputFile, pdfFile, "SketchUp", "SKP", suggestions);
    }
}
