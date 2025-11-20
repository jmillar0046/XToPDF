package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.text.BadLocationException;
import javax.swing.text.rtf.RTFEditorKit;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Service to convert RTF (Rich Text Format) files to PDF.
 * Uses RTFEditorKit to parse RTF and extract plain text.
 * Uses the PDF backend abstraction layer with Apache PDFBox.
 */
@Service
public class RtfToPdfService {
    
    private final PdfBackendProvider pdfBackend;
    
    public RtfToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }
    
    public void convertRtfToPdf(MultipartFile rtfFile, File pdfFile) throws IOException {
        // Read the RTF file content using RTFEditorKit
        RTFEditorKit rtfKit = new RTFEditorKit();
        javax.swing.text.Document rtfDocument = rtfKit.createDefaultDocument();
        
        try (InputStreamReader reader = new InputStreamReader(rtfFile.getInputStream())) {
            rtfKit.read(reader, rtfDocument, 0);
            
            // Extract plain text from the RTF document
            String textContent = rtfDocument.getText(0, rtfDocument.getLength());
            
            // Create PDF using abstraction layer (PDFBox backend)
            try (PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
                builder.addParagraph(textContent);
                builder.save(pdfFile);
            }
        } catch (BadLocationException e) {
            throw new IOException("Error parsing RTF content: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new IOException("Error creating PDF from RTF: " + e.getMessage(), e);
        }
    }
}