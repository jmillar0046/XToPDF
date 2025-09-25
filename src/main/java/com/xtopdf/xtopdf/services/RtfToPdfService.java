package com.xtopdf.xtopdf.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import javax.swing.text.BadLocationException;
import javax.swing.text.rtf.RTFEditorKit;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class RtfToPdfService {
    
    public void convertRtfToPdf(MultipartFile rtfFile, File pdfFile) throws IOException {
        // Read the RTF file content using RTFEditorKit
        RTFEditorKit rtfKit = new RTFEditorKit();
        javax.swing.text.Document rtfDocument = rtfKit.createDefaultDocument();
        
        try (InputStreamReader reader = new InputStreamReader(rtfFile.getInputStream())) {
            rtfKit.read(reader, rtfDocument, 0);
            
            // Extract plain text from the RTF document
            String textContent = rtfDocument.getText(0, rtfDocument.getLength());
            
            // Create a PDF document using iText
            try (PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile))) {
                PdfDocument pdfDocument = new PdfDocument(writer);
                Document document = new Document(pdfDocument);
                
                // Add the text content as a paragraph to the PDF
                document.add(new Paragraph(textContent));
                
                document.close();
            }
        } catch (BadLocationException e) {
            throw new IOException("Error parsing RTF content: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new IOException("Error creating PDF from RTF: " + e.getMessage(), e);
        }
    }
}