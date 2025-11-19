package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Service for converting JSON files to PDF.
 * Uses the PDF backend abstraction layer with Apache PDFBox.
 */
@Service
public class JsonToPdfService {
    
    private final PdfBackendProvider pdfBackend;
    
    public JsonToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }
    
    public void convertJsonToPdf(MultipartFile jsonFile, File pdfFile) throws IOException {
        // Read the JSON file content
        StringBuilder jsonContent = new StringBuilder();
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(jsonFile.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                jsonContent.append(line).append("\n");
            }
        }

        // Create PDF using abstraction layer (PDFBox backend)
        try (PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
            builder.addParagraph(jsonContent.toString());
            builder.save(pdfFile);
        } catch (Exception e) {
            throw new IOException("Error creating PDF from JSON: " + e.getMessage(), e);
        }
    }
}
