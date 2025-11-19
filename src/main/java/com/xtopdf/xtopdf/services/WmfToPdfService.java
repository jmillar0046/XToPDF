package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Service to convert Windows Metafile files (WMF) to PDF.
 * WMF is a binary vector graphics format for Windows.
 * This converter parses the WMF header and provides file statistics.
 * Uses the PDF backend abstraction layer with Apache PDFBox.
 */
@Service
public class WmfToPdfService {
    
    private final PdfBackendProvider pdfBackend;
    
    public WmfToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }
    
    public void convertWmfToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        try (PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
            // Parse WMF file
            WmfFileData wmfData = parseWmfFile(inputFile);
            
            // Build PDF content
            StringBuilder content = new StringBuilder();
            content.append("Windows Metafile Analysis\n\n");
            content.append("File: ").append(inputFile.getOriginalFilename()).append("\n");
            content.append("Format: WMF (Windows Metafile)\n");
            content.append("Type: ").append(wmfData.isPlaceable ? "Placeable" : "Standard").append("\n\n");
            content.append("Metafile Statistics:\n");
            content.append("File Size: ").append(formatSize(inputFile.getSize())).append("\n");
            
            if (wmfData.isPlaceable && wmfData.boundsValid) {
                content.append("\nBounds:\n");
                content.append(String.format("  Left: %d, Top: %d\n", wmfData.boundsLeft, wmfData.boundsTop));
                content.append(String.format("  Right: %d, Bottom: %d\n", wmfData.boundsRight, wmfData.boundsBottom));
                content.append(String.format("  Width: %d, Height: %d\n", 
                    wmfData.boundsRight - wmfData.boundsLeft, 
                    wmfData.boundsBottom - wmfData.boundsTop));
            }
            
            if (wmfData.maxRecordSize > 0) {
                content.append("\nMax Record Size: ").append(wmfData.maxRecordSize).append(" words\n");
            }
            
            content.append("\nNote: This PDF contains metafile statistics. For visual rendering, use image conversion tools or Windows applications.");
            
            builder.addParagraph(content.toString());
            builder.save(pdfFile);
        } catch (Exception e) {
            throw new IOException("Error converting WMF to PDF: " + e.getMessage(), e);
        }
    }
    
    private WmfFileData parseWmfFile(MultipartFile file) throws IOException {
        WmfFileData data = new WmfFileData();
        
        byte[] bytes = file.getBytes();
        if (bytes.length < 18) { // Minimum WMF header size
            return data;
        }
        
        try {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            
            // Check for placeable metafile header (magic number 0x9AC6CDD7)
            int magic = buffer.getInt();
            if (magic == 0x9AC6CDD7) {
                data.isPlaceable = true;
                
                // Skip handle (2 bytes)
                buffer.getShort();
                
                // Read bounding rectangle
                data.boundsLeft = buffer.getShort();
                data.boundsTop = buffer.getShort();
                data.boundsRight = buffer.getShort();
                data.boundsBottom = buffer.getShort();
                data.boundsValid = true;
                
                // Skip inch (2 bytes) and reserved (4 bytes) and checksum (2 bytes)
                buffer.position(buffer.position() + 8);
            } else {
                // Standard WMF, rewind
                buffer.position(0);
            }
            
            // Read standard WMF header
            int fileType = buffer.getShort() & 0xFFFF;
            int headerSize = buffer.getShort() & 0xFFFF;
            
            // Skip version (2 bytes)
            buffer.getShort();
            
            // Read file size in words
            int fileSizeWords = buffer.getInt();
            
            // Skip number of objects (2 bytes)
            buffer.getShort();
            
            // Read max record size
            data.maxRecordSize = buffer.getInt();
            
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
    
    private static class WmfFileData {
        boolean isPlaceable = false;
        boolean boundsValid = false;
        int boundsLeft = 0;
        int boundsTop = 0;
        int boundsRight = 0;
        int boundsBottom = 0;
        int maxRecordSize = 0;
    }
}
