package com.xtopdf.xtopdf.services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.List;
import com.itextpdf.layout.element.ListItem;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Generic service for converting proprietary CAD formats to informational PDF.
 * Used for formats that require specialized software or libraries.
 */
@Service
public class ProprietaryCadToPdfService {
    
    public void convertToPdf(MultipartFile inputFile, File pdfFile, String formatName, String formatDescription, String[] suggestions) throws IOException {
        try (PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile))) {
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);
            
            document.add(new Paragraph(formatName + " File")
                .setFontSize(18)
                
                .setMarginBottom(10));
            
            document.add(new Paragraph("File: " + inputFile.getOriginalFilename()).setFontSize(12));
            document.add(new Paragraph("Format: " + formatDescription).setFontSize(12));
            document.add(new Paragraph("Size: " + formatSize(inputFile.getSize())).setFontSize(12));
            document.add(new Paragraph(""));
            
            document.add(new Paragraph("This is a proprietary CAD format that requires specialized software for visualization and conversion.").setFontSize(11));
            document.add(new Paragraph(""));
            
            if (suggestions != null && suggestions.length > 0) {
                document.add(new Paragraph("Suggested conversion options:").setFontSize(12));
                List list = new List();
                for (String suggestion : suggestions) {
                    ListItem item = new ListItem(suggestion);
                    item.setFontSize(10);
                    list.add(item);
                }
                document.add(list);
            }
            
            document.close();
        } catch (Exception e) {
            throw new IOException("Error converting " + formatName + " to PDF: " + e.getMessage(), e);
        }
    }
    
    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " bytes";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }
}
