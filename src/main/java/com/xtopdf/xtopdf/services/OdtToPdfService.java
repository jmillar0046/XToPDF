package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import lombok.extern.slf4j.Slf4j;
import org.odftoolkit.odfdom.doc.OdfTextDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
@Slf4j
public class OdtToPdfService {
    
    private final PdfBackendProvider pdfBackend;
    
    public OdtToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }
    
    public void convertOdtToPdf(MultipartFile odtFile, File pdfFile) throws IOException {
        try (var fis = odtFile.getInputStream();
             PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
            
            OdfTextDocument odtDocument = OdfTextDocument.loadDocument(fis);
            
            String textContent = odtDocument.getContentRoot().getTextContent();
            
            if (textContent != null && !textContent.isEmpty()) {
                String[] lines = textContent.split("\n");
                for (String line : lines) {
                    if (!line.trim().isEmpty()) {
                        builder.addParagraph(line);
                    }
                }
            }
            
            builder.save(pdfFile);
        } catch (Exception e) {
            log.error("Error processing ODT file: {}", e.getMessage(), e);
            throw new IOException("Error processing ODT file: " + e.getMessage(), e);
        }
    }
}
