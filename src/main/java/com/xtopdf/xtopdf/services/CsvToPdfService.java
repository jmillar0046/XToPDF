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
 * Service for converting CSV files to PDF.
 * Uses the PDF backend abstraction layer with Apache PDFBox.
 */
@Slf4j
@Service
public class CsvToPdfService {
    
    // Security: Maximum allowed values to prevent DoS attacks
    private static final int MAX_LINE_LENGTH = 1_000_000;  // 1MB per line
    private static final int MAX_FIELDS = 10_000;          // 10k fields per row
    private static final long MAX_FILE_SIZE = 100_000_000; // 100MB
    
    private final PdfBackendProvider pdfBackend;
    
    public CsvToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }
    
    public void convertCsvToPdf(MultipartFile csvFile, File pdfFile) throws IOException {
        log.debug("Starting CSV to PDF conversion for file: {}", csvFile.getOriginalFilename());
        
        // Validate file size
        long fileSize = csvFile.getSize();
        if (fileSize > MAX_FILE_SIZE) {
            log.warn("CSV file exceeds maximum size: {} bytes (max: {})", fileSize, MAX_FILE_SIZE);
            throw new IOException("File size exceeds maximum allowed: " + MAX_FILE_SIZE + " bytes");
        }
        
        // Read CSV content with validation
        List<String[]> rows = new ArrayList<>();
        int maxColumns = 0;
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(csvFile.getInputStream()))) {
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
                
                String[] values = parseCsvLine(line, lineNumber);
                
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
            log.warn("CSV file is empty: {}", csvFile.getOriginalFilename());
            throw new IOException("CSV file is empty");
        }

        log.debug("Parsed {} rows with max {} columns from CSV file", rows.size(), maxColumns);

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
            log.info("Successfully converted CSV to PDF: {} -> {}", csvFile.getOriginalFilename(), pdfFile.getName());
        } catch (Exception e) {
            log.error("Error creating PDF from CSV: {}", e.getMessage(), e);
            throw new IOException("Error creating PDF from CSV: " + e.getMessage(), e);
        }
    }
    
    /**
     * Parse a CSV line handling quoted values and escaped quotes
     */
    String[] parseCsvLine(String line, int lineNumber) {
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
            } else if (c == ',' && !inQuotes) {
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
