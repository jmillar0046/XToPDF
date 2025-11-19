package com.xtopdf.xtopdf.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Service to convert Inventor Assembly files (IAM) to PDF.
 */
@AllArgsConstructor
@Service
public class IamToPdfService {
    private final ProprietaryCadToPdfService proprietaryCadService;
    
    public void convertIamToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        String[] suggestions = {"Use Autodesk Inventor to export to PDF or STEP", "Use Inventor View for visualization"};
        proprietaryCadService.convertToPdf(inputFile, pdfFile, "Inventor Assembly", "IAM", suggestions);
    }
}
