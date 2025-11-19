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
 * Service for converting XML files to PDF.
 * Uses the PDF backend abstraction layer with Apache PDFBox.
 */
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
