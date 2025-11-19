package com.xtopdf.xtopdf.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Service to convert Rhino 3D files (3DM) to PDF.
 */
@AllArgsConstructor
@Service
public class ThreeDmToPdfService {
    private final ProprietaryCadToPdfService proprietaryCadService;
    
    public void convertThreeDmToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        String[] suggestions = {"Use Rhino to export to PDF or STEP", "Use Rhino Viewer for visualization"};
        proprietaryCadService.convertToPdf(inputFile, pdfFile, "Rhino 3D", "3DM", suggestions);
    }
}
