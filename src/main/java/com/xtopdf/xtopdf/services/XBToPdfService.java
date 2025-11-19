package com.xtopdf.xtopdf.services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Service to convert X_B (Parasolid Binary) files to PDF.
 * Parasolid is a geometric modeling kernel format.
 */
@Service
public class XBToPdfService {
    
    public void convertXBToPdf(MultipartFile xbFile, File pdfFile) throws IOException {
        try (PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile))) {
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);
            
            document.add(new Paragraph("Parasolid Binary File")
                .setFontSize(18)
                
                .setMarginBottom(10));
            
            document.add(new Paragraph("File: " + xbFile.getOriginalFilename()).setFontSize(12));
            document.add(new Paragraph("Format: Parasolid Binary (.x_b)").setFontSize(12));
            document.add(new Paragraph("Size: " + formatSize(xbFile.getSize())).setFontSize(12));
            document.add(new Paragraph(""));
            
            document.add(new Paragraph("This is a Parasolid geometric modeling format commonly used in CAD systems like SolidWorks, NX, and SolidEdge.").setFontSize(11));
            document.add(new Paragraph(""));
            
            document.add(new Paragraph("Note: Full visualization requires CAD software that supports Parasolid format.").setFontSize(10));
            
            document.close();
        } catch (Exception e) {
            throw new IOException("Error converting X_B to PDF: " + e.getMessage(), e);
        }
    }
    
    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " bytes";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }
}
