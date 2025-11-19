package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Service to convert Enhanced Metafile files (EMF) to PDF.
 * EMF is a binary vector graphics format for Windows.
 * This converter parses the EMF header and provides file statistics.
 * Uses the PDF backend abstraction layer with Apache PDFBox.
 */
@Service
public class EmfToPdfService {
    
    private final PdfBackendProvider pdfBackend;
    
    public EmfToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }
    
    public void convertEmfToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        try (PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
            // Parse EMF file
            EmfFileData emfData = parseEmfFile(inputFile);
            
            // Build PDF content
            StringBuilder content = new StringBuilder();
            content.append("Enhanced Metafile Analysis\n\n");
            content.append("File: ").append(inputFile.getOriginalFilename()).append("\n");
            content.append("Format: EMF (Enhanced Metafile)\n\n");
            content.append("Metafile Statistics:\n");
            content.append("File Size: ").append(formatSize(inputFile.getSize())).append("\n");
            content.append("Record Count: ").append(emfData.recordCount).append("\n");
            
            if (emfData.boundsValid) {
                content.append("\nBounds:\n");
                content.append(String.format("  Left: %d, Top: %d\n", emfData.boundsLeft, emfData.boundsTop));
                content.append(String.format("  Right: %d, Bottom: %d\n", emfData.boundsRight, emfData.boundsBottom));
                content.append(String.format("  Width: %d, Height: %d\n", 
                    emfData.boundsRight - emfData.boundsLeft, 
                    emfData.boundsBottom - emfData.boundsTop));
            }
            
            content.append("\nNote: This PDF contains metafile statistics. For visual rendering, use image conversion tools or Windows applications.");
            
            builder.addParagraph(content.toString());
            builder.save(pdfFile);
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
