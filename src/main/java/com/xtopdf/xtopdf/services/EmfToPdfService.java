package com.xtopdf.xtopdf.services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Service to convert Enhanced Metafile files (EMF) to PDF.
 * EMF is a binary vector graphics format for Windows.
 * This converter parses the EMF header and provides file statistics.
 */
@Service
public class EmfToPdfService {
    
    public void convertEmfToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        try (PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile))) {
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);
            
            // Parse EMF file
            EmfFileData emfData = parseEmfFile(inputFile);
            
            // Add title
            document.add(new Paragraph("Enhanced Metafile Analysis")
                .setFontSize(18)
                .setMarginBottom(10));
            
            // Add file information
            document.add(new Paragraph("File: " + inputFile.getOriginalFilename()).setFontSize(12));
            document.add(new Paragraph("Format: EMF (Enhanced Metafile)").setFontSize(12));
            document.add(new Paragraph(""));
            
            // Add metafile statistics
            document.add(new Paragraph("Metafile Statistics:").setFontSize(14));
            document.add(new Paragraph("File Size: " + formatSize(inputFile.getSize())).setFontSize(12));
            document.add(new Paragraph("Record Count: " + emfData.recordCount).setFontSize(12));
            
            if (emfData.boundsValid) {
                document.add(new Paragraph(""));
                document.add(new Paragraph("Bounds:").setFontSize(12));
                document.add(new Paragraph(String.format("  Left: %d, Top: %d", emfData.boundsLeft, emfData.boundsTop)).setFontSize(10));
                document.add(new Paragraph(String.format("  Right: %d, Bottom: %d", emfData.boundsRight, emfData.boundsBottom)).setFontSize(10));
                document.add(new Paragraph(String.format("  Width: %d, Height: %d", 
                    emfData.boundsRight - emfData.boundsLeft, 
                    emfData.boundsBottom - emfData.boundsTop)).setFontSize(10));
            }
            
            document.add(new Paragraph(""));
            document.add(new Paragraph("Note: This PDF contains metafile statistics. For visual rendering, use image conversion tools or Windows applications.").setFontSize(10));
            
            document.close();
        } catch (Exception e) {
            throw new IOException("Error converting EMF to PDF: " + e.getMessage(), e);
        }
    }
    
    private EmfFileData parseEmfFile(MultipartFile file) throws IOException {
        EmfFileData data = new EmfFileData();
        
        byte[] bytes = file.getBytes();
        if (bytes.length < 88) { // Minimum EMF header size
            return data;
        }
        
        try {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            
            // Read EMR_HEADER (type should be 1)
            int recordType = buffer.getInt();
            if (recordType != 1) {
                return data; // Not a valid EMF file
            }
            
            // Skip record size
            buffer.getInt();
            
            // Read bounds rectangle
            data.boundsLeft = buffer.getInt();
            data.boundsTop = buffer.getInt();
            data.boundsRight = buffer.getInt();
            data.boundsBottom = buffer.getInt();
            data.boundsValid = true;
            
            // Skip frame rectangle (16 bytes)
            buffer.position(buffer.position() + 16);
            
            // Skip signature (4 bytes)
            buffer.getInt();
            
            // Skip version (4 bytes)
            buffer.getInt();
            
            // Skip file size (4 bytes)
            buffer.getInt();
            
            // Read number of records
            data.recordCount = buffer.getInt();
            
        } catch (Exception e) {
            // If parsing fails, return what we have
        }
        
        return data;
    }
    
    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " bytes";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }
    
    private static class EmfFileData {
        int recordCount = 0;
        boolean boundsValid = false;
        int boundsLeft = 0;
        int boundsTop = 0;
        int boundsRight = 0;
        int boundsBottom = 0;
    }
}
