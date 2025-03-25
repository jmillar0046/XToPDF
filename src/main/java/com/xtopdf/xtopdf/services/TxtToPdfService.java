package com.xtopdf.xtopdf.services;

import org.springframework.stereotype.Service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class TxtToPdfService {
     public void convertTxtToPdf(File txtFile, File pdfFile) throws IOException {
       // Read the .txt file content
        BufferedReader br = new BufferedReader(new FileReader(txtFile));
        String line;
        StringBuilder textContent = new StringBuilder();

        while ((line = br.readLine()) != null) {
            textContent.append(line).append("\n");
        }
        br.close();

        // Create a PDF document using iText
        try (PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile))) {
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);
            
            // Add the text content as a paragraph to the PDF
            document.add(new Paragraph(textContent.toString()));
            
            document.close();
        } catch (Exception e) {
            throw new IOException("Error creating PDF from a .txt: " + e.getMessage());
        }
    }
}