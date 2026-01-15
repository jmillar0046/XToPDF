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
 * Service for converting XML files to PDF.
 * Uses the PDF backend abstraction layer with Apache PDFBox.
 */
@Slf4j
@Service
public class XmlToPdfService {
    
    private final PdfBackendProvider pdfBackend;
    
    public XmlToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }
    
    public void convertXmlToPdf(MultipartFile xmlFile, File pdfFile) throws IOException {
        // Read the XML file content
        StringBuilder xmlContent = new StringBuilder();
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(xmlFile.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                xmlContent.append(line).append("\n");
            }
        }

        // Create PDF using abstraction layer (PDFBox backend)
        try (PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
            builder.addParagraph(xmlContent.toString());
            builder.save(pdfFile);
        } catch (Exception e) {
            throw new IOException("Error creating PDF from XML: " + e.getMessage(), e);
        }
    }
}
