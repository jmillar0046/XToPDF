package com.xtopdf.xtopdf.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Service to convert Revit Family files (RFA) to PDF.
 */
@AllArgsConstructor
@Service
public class RfaToPdfService {
    private final ProprietaryCadToPdfService proprietaryCadService;
    
    public void convertRfaToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        String[] suggestions = {"Load in Revit and export to PDF", "Use Revit Viewer for visualization"};
        proprietaryCadService.convertToPdf(inputFile, pdfFile, "Revit Family", "RFA", suggestions);
    }
}
