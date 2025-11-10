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
public class JsonToPdfService {
    public void convertJsonToPdf(MultipartFile jsonFile, File pdfFile) throws IOException {
        // Read the JSON file content
        StringBuilder jsonContent = new StringBuilder();
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(jsonFile.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                jsonContent.append(line).append("\n");
            }
        }

        // Create a PDF document using iText
        try (PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile))) {
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);
            
            // Add the JSON content as a paragraph to the PDF with monospace formatting
            document.add(new Paragraph(jsonContent.toString()));
            
            document.close();
        } catch (Exception e) {
            throw new IOException("Error creating PDF from JSON: " + e.getMessage(), e);
        }
    }
}
