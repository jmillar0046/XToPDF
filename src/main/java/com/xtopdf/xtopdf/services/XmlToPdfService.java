package com.xtopdf.xtopdf.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class XmlToPdfService {
    public void convertXmlToPdf(MultipartFile xmlFile, File pdfFile) throws IOException {
        // Read the XML file content
        StringBuilder xmlContent = new StringBuilder();
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(xmlFile.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                xmlContent.append(line).append("\n");
            }
        }

        // Create a PDF document using iText
        try (PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile))) {
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);
            
            // Add the XML content as a paragraph to the PDF with monospace formatting
            document.add(new Paragraph(xmlContent.toString()));
            
            document.close();
        } catch (Exception e) {
            throw new IOException("Error creating PDF from XML: " + e.getMessage(), e);
        }
    }
}
