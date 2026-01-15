package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for converting TSV files to PDF.
 * Uses the PDF backend abstraction layer with Apache PDFBox.
 */
@Slf4j
@Service
public class TsvToPdfService {
    
    // Security: Maximum allowed values to prevent DoS attacks
    private static final int MAX_LINE_LENGTH = 1_000_000;  // 1MB per line
    private static final int MAX_FIELDS = 10_000;          // 10k fields per row
    private static final long MAX_FILE_SIZE = 100_000_000; // 100MB
    
    private final PdfBackendProvider pdfBackend;
    
    public TsvToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }
    
    public void convertTsvToPdf(MultipartFile tsvFile, File pdfFile) throws IOException {
        log.debug("Starting TSV to PDF conversion for file: {}", tsvFile.getOriginalFilename());
        
        // Validate file size
        long fileSize = tsvFile.getSize();
        if (fileSize > MAX_FILE_SIZE) {
            log.warn("TSV file exceeds maximum size: {} bytes (max: {})", fileSize, MAX_FILE_SIZE);
            throw new IOException("File size exceeds maximum allowed: " + MAX_FILE_SIZE + " bytes");
        }
        
        // Read TSV content with validation
        List<String[]> rows = new ArrayList<>();
        int maxColumns = 0;
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(tsvFile.getInputStream()))) {
            String line;
            int lineNumber = 0;
            
            while ((line = br.readLine()) != null) {
                lineNumber++;
                
                // Validate line length
                if (line.length() > MAX_LINE_LENGTH) {
                    log.warn("Line {} exceeds maximum length: {} chars (max: {})", 
                             lineNumber, line.length(), MAX_LINE_LENGTH);
                    throw new IOException("Line " + lineNumber + " exceeds maximum length: " + MAX_LINE_LENGTH);
                }
                
                String[] values = parseTsvLine(line, lineNumber);
                
                // Validate field count
                if (values.length > MAX_FIELDS) {
                    log.warn("Line {} exceeds maximum field count: {} fields (max: {})", 
                             lineNumber, values.length, MAX_FIELDS);
                    throw new IOException("Line " + lineNumber + " exceeds maximum field count: " + MAX_FIELDS);
                }
                
                rows.add(values);
                maxColumns = Math.max(maxColumns, values.length);
            }
        }

        if (rows.isEmpty()) {
            log.warn("TSV file is empty: {}", tsvFile.getOriginalFilename());
            throw new IOException("TSV file is empty");
        }

        log.debug("Parsed {} rows with max {} columns from TSV file", rows.size(), maxColumns);

        // Normalize rows to have same number of columns
        String[][] tableData = new String[rows.size()][maxColumns];
        for (int i = 0; i < rows.size(); i++) {
            String[] row = rows.get(i);
            for (int j = 0; j < maxColumns; j++) {
                tableData[i][j] = j < row.length ? row[j] : "";
            }
        }

        // Create PDF using abstraction layer (PDFBox backend)
        try (PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
            builder.addTable(tableData);
            builder.save(pdfFile);
            log.info("Successfully converted TSV to PDF: {} -> {}", tsvFile.getOriginalFilename(), pdfFile.getName());
        } catch (Exception e) {
            log.error("Error creating PDF from TSV: {}", e.getMessage(), e);
            throw new IOException("Error creating PDF from TSV: " + e.getMessage(), e);
        }
    }
    
    /**
     * Parse a TSV line handling quoted values and escaped quotes
     */
    String[] parseTsvLine(String line, int lineNumber) {
        List<String> values = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // Escaped quote
                    currentValue.append('"');
                    i++; // Skip next quote
                } else {
                    // Toggle quote state
                    inQuotes = !inQuotes;
                }
            } else if (c == '\t' && !inQuotes) {
                // End of field
                values.add(currentValue.toString());
                currentValue = new StringBuilder();
            } else {
                currentValue.append(c);
            }
        }
        
        // Handle unclosed quotes at end of line
        if (inQuotes) {
            log.warn("Unclosed quote in line {}, treating as literal", lineNumber);
        }
        
        // Add last value
        values.add(currentValue.toString());
        
        return values.toArray(new String[0]);
    }
}
