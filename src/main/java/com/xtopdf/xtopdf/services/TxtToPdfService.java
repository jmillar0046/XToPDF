package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import lombok.extern.slf4j.Slf4j;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import lombok.extern.slf4j.Slf4j;
import java.io.File;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import java.io.InputStreamReader;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for converting TXT files to PDF.
 * Uses the PDF backend abstraction layer with Apache PDFBox.
 */
@Slf4j
@Service
public class TxtToPdfService {
    
    private final PdfBackendProvider pdfBackend;
    
    public TxtToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }
    
    public void convertTxtToPdf(MultipartFile txtFile, File pdfFile) throws IOException {
        // Read the .txt file content
        StringBuilder textContent = new StringBuilder();
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(txtFile.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                textContent.append(line).append("\n");
            }
        }

        // Create PDF using abstraction layer (PDFBox backend)
        try (PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
            builder.addParagraph(textContent.toString());
            builder.save(pdfFile);
        } catch (Exception e) {
            throw new IOException("Error creating PDF from .txt file: " + e.getMessage(), e);
        }
    }
}