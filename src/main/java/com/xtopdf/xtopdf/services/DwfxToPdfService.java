package com.xtopdf.xtopdf.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Service to convert Design Web Format XPS files (DWFX) to PDF.
 * DWFX is based on the XPS format, delegates to DwfToPdfService for similar parsing.
 */
@AllArgsConstructor
@Service
public class DwfxToPdfService {
    private final DwfToPdfService dwfToPdfService;
    
    public void convertDwfxToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        dwfToPdfService.convertDwfToPdf(inputFile, pdfFile);
    }
}
