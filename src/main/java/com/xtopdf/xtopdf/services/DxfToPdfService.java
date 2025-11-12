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

/**
 * Service to convert DXF (Drawing Exchange Format) files to PDF.
 * 
 * Note: This is a basic implementation that treats DXF as a text-based format.
 * For production use with complex DXF files containing advanced CAD entities,
 * consider using specialized libraries like:
 * - Kabeja (open source, but requires manual installation)
 * - Aspose.CAD (commercial)
 * - Converting DXF to SVG first, then SVG to PDF
 */
@Service
public class DxfToPdfService {
    
    public void convertDxfToPdf(MultipartFile dxfFile, File pdfFile) throws IOException {
        // DXF is a text-based format, so we can read it as text
        // This is a basic implementation that preserves the DXF structure as text
        StringBuilder dxfContent = new StringBuilder();
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(dxfFile.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                dxfContent.append(line).append("\n");
            }
        }
        
        // Create a PDF document using iText
        try (PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile))) {
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);
            
            // Add the DXF content as a paragraph to the PDF
            // Using a monospace font would be ideal for preserving formatting
            document.add(new Paragraph(dxfContent.toString())
                .setFont(com.itextpdf.kernel.font.PdfFontFactory.createFont(
                    com.itextpdf.io.font.constants.StandardFonts.COURIER))
                .setFontSize(8));
            
            document.close();
        } catch (Exception e) {
            throw new IOException("Error creating PDF from DXF: " + e.getMessage(), e);
        }
    }
}
