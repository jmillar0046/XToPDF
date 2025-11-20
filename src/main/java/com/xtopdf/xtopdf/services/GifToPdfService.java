package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Service for converting GIF files to PDF.
 * Uses the PDF backend abstraction layer with Apache PDFBox.
 */
@Service
public class GifToPdfService {
    
    private final PdfBackendProvider pdfBackend;
    
    public GifToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }
    
    public void convertGifToPdf(MultipartFile gifFile, File pdfFile) throws IOException {
        try {
            byte[] imageBytes = gifFile.getBytes();
            
            // Create PDF using abstraction layer (PDFBox backend)
            try (PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
                builder.addImage(imageBytes);
                builder.save(pdfFile);
            }
        } catch (Exception e) {
            throw new IOException("Error converting GIF to PDF: " + e.getMessage(), e);
        }
    }
}
