package com.xtopdf.xtopdf.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Service to convert Windows Metafile files (WMF) to PDF.
 */
@AllArgsConstructor
@Service
public class WmfToPdfService {
    private final ProprietaryCadToPdfService proprietaryCadService;
    
    public void convertWmfToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        String[] suggestions = {"Use image conversion tools like ImageMagick", "Open in Windows apps and print to PDF"};
        proprietaryCadService.convertToPdf(inputFile, pdfFile, "Windows Metafile", "WMF", suggestions);
    }
}
