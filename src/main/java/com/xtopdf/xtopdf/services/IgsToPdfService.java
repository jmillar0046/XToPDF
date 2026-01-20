package com.xtopdf.xtopdf.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Service to convert IGS files to PDF.
 * IGS is an alias for IGES format.
 */
@AllArgsConstructor
@Slf4j
@Service
public class IgsToPdfService {
    private final IgesToPdfService igesToPdfService;
    
    public void convertIgsToPdf(MultipartFile igsFile, File pdfFile) throws IOException {
        igesToPdfService.convertIgesToPdf(igsFile, pdfFile);
    }
}
