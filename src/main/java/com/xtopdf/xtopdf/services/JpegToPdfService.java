package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import lombok.extern.slf4j.Slf4j;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for converting JPEG files to PDF.
 * Uses the PDF backend abstraction layer with Apache PDFBox.
 */
@Slf4j
@Service
public class JpegToPdfService {
    
    private final PdfBackendProvider pdfBackend;
    
    public JpegToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }
    
    public void convertJpegToPdf(MultipartFile jpegFile, File pdfFile) throws IOException {
        try {
            byte[] imageBytes = jpegFile.getBytes();
            
            if (imageBytes == null || imageBytes.length == 0) {
                throw new IOException("JPEG file is empty or invalid");
            }
            
            // Create PDF using abstraction layer (PDFBox backend)
            try (PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
                builder.addImage(imageBytes);
                builder.save(pdfFile);
            }
        } catch (Exception e) {
            throw new IOException("Error converting JPEG to PDF: " + e.getMessage(), e);
        }
    }
}