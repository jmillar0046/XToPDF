package com.xtopdf.xtopdf.services;

import java.io.File;
import java.io.IOException;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import lombok.extern.slf4j.Slf4j;
import org.odftoolkit.odfdom.doc.OdfTextDocument;
import org.odftoolkit.odfdom.doc.table.OdfTable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class OdtToPdfService {
    public void convertOdtToPdf(MultipartFile odtFile, File pdfFile) throws IOException {
        try (var fis = odtFile.getInputStream();
             PdfWriter writer = new PdfWriter(pdfFile)) {
            
            OdfTextDocument odtDocument = OdfTextDocument.loadDocument(fis);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document pdfDoc = new Document(pdfDocument);
            
            // Extract text content from the ODT document
            String textContent = odtDocument.getContentRoot().getTextContent();
            
            if (textContent != null && !textContent.isEmpty()) {
                // Split by newlines and add as paragraphs
                String[] lines = textContent.split("\n");
                for (String line : lines) {
                    if (!line.trim().isEmpty()) {
                        pdfDoc.add(new Paragraph(line));
                    }
                }
            }
            
            pdfDoc.close();
        } catch (Exception e) {
            log.error("Error processing ODT file: {}", e.getMessage(), e);
            throw new IOException("Error processing ODT file: " + e.getMessage(), e);
        }
    }
}
