package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Service for converting PNG files to PDF.
 * Uses the PDF backend abstraction layer with Apache PDFBox.
 */
@Service
public class PngToPdfService {
    
    private final PdfBackendProvider pdfBackend;
    
    public PngToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }
    
    public void convertPngToPdf(MultipartFile pngFile, File pdfFile) throws IOException {
        if (pngFile == null) {
            throw new NullPointerException("PNG file cannot be null");
        }
        if (pdfFile == null) {
            throw new NullPointerException("Output PDF file cannot be null");
        }
        
        try {
            byte[] imageBytes = pngFile.getBytes();
            if (imageBytes == null || imageBytes.length == 0) {
                throw new IOException("PNG file is empty or invalid");
            }
            
            // Create PDF using abstraction layer (PDFBox backend)
            try (PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
                builder.addImage(imageBytes);
                builder.save(pdfFile);
            }
        } catch (Exception e) {
            throw new IOException("Error creating PDF from PNG: " + e.getMessage(), e);
        }
    }
}