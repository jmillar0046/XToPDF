package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import lombok.extern.slf4j.Slf4j;
import org.odftoolkit.odfdom.doc.OdfPresentationDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
@Slf4j
public class OdpToPdfService {
    
    private final PdfBackendProvider pdfBackend;
    
    public OdpToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }
    
    public void convertOdpToPdf(MultipartFile odpFile, File pdfFile) throws IOException {
        try (var fis = odpFile.getInputStream();
             PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
            
            OdfPresentationDocument odpDocument = OdfPresentationDocument.loadDocument(fis);
            
            String textContent = odpDocument.getContentRoot().getTextContent();
            
            if (textContent != null && !textContent.isEmpty()) {
                builder.addParagraph("Presentation Content:\n\n");
                String[] lines = textContent.split("\n");
                for (String line : lines) {
                    if (!line.trim().isEmpty()) {
                        builder.addParagraph(line);
                    }
                }
            } else {
                builder.addParagraph("(No text content extracted from presentation)");
            }
            
            builder.save(pdfFile);
        } catch (Exception e) {
            log.error("Error processing ODP file: {}", e.getMessage(), e);
            throw new IOException("Error processing ODP file: " + e.getMessage(), e);
        }
    }
}
