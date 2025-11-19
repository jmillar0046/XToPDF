package com.xtopdf.xtopdf.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Service to convert Fusion 360 files (F3D) to PDF.
 */
@AllArgsConstructor
@Service
public class F3dToPdfService {
    private final ProprietaryCadToPdfService proprietaryCadService;
    
    public void convertF3dToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        String[] suggestions = {"Use Fusion 360 to export to PDF or STEP", "Export from Fusion 360 cloud platform"};
        proprietaryCadService.convertToPdf(inputFile, pdfFile, "Fusion 360", "F3D", suggestions);
    }
}
