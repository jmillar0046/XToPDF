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
    
    private final PdfBackendProvider pdfBackend;
    
    public TsvToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }
    
    public void convertTsvToPdf(MultipartFile tsvFile, File pdfFile) throws IOException {
        log.debug("Starting TSV to PDF conversion for file: {}", tsvFile.getOriginalFilename());
        
        // Read TSV content
        List<String[]> rows = new ArrayList<>();
        int maxColumns = 0;
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(tsvFile.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = parseTsvLine(line);
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
    String[] parseTsvLine(String line) {
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
        
        // Add last value
        values.add(currentValue.toString());
        
        return values.toArray(new String[0]);
    }
}
