package com.xtopdf.xtopdf.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvToPdfService {
    public void convertCsvToPdf(MultipartFile csvFile, File pdfFile) throws IOException {
        // Read CSV content
        List<String[]> rows = new ArrayList<>();
        int maxColumns = 0;
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(csvFile.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = parseCsvLine(line);
                rows.add(values);
                maxColumns = Math.max(maxColumns, values.length);
            }
        }

        if (rows.isEmpty()) {
            throw new IOException("CSV file is empty");
        }

        // Create PDF document
        try (PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile))) {
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);
            
            // Create table with appropriate number of columns
            Table table = new Table(UnitValue.createPercentArray(maxColumns)).useAllAvailableWidth();
            
            // Add rows to table
            for (String[] row : rows) {
                for (int i = 0; i < maxColumns; i++) {
                    String cellValue = i < row.length ? row[i] : "";
                    table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(cellValue)));
                }
            }
            
            document.add(table);
            document.close();
        } catch (Exception e) {
            throw new IOException("Error creating PDF from CSV: " + e.getMessage(), e);
        }
    }
    
    /**
     * Parse a CSV line handling quoted values and escaped quotes
     */
    String[] parseCsvLine(String line) {
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
        
        // Add last value
        values.add(currentValue.toString());
        
        return values.toArray(new String[0]);
    }
}
