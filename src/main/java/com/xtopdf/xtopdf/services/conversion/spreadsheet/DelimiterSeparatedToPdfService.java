package com.xtopdf.xtopdf.services.conversion.spreadsheet;

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
 * Unified service for converting delimiter-separated files (CSV, TSV, etc.) to PDF.
 * Accepts a delimiter character parameter to handle any delimiter-separated format.
 * Uses the PDF backend abstraction layer with Apache PDFBox.
 *
 * This service replaces the separate CsvToPdfService and TsvToPdfService with a
 * single parameterized implementation, ensuring bug fixes and improvements apply
 * to all delimiter-separated formats automatically.
 */
@Slf4j
@Service
public class DelimiterSeparatedToPdfService {

    // Security: Maximum allowed values to prevent DoS attacks
    private static final int MAX_LINE_LENGTH = 1_000_000;  // 1MB per line
    private static final int MAX_FIELDS = 10_000;          // 10k fields per row
    private static final long MAX_FILE_SIZE = 100_000_000; // 100MB

    // Performance: Streaming thresholds for large files
    private static final long STREAMING_THRESHOLD = 10_000_000; // 10MB
    private static final int CHUNK_SIZE = 1000; // Process 1000 rows at a time

    private final PdfBackendProvider pdfBackend;

    public DelimiterSeparatedToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }

    public void convertDelimiterSeparatedToPdf(MultipartFile inputFile, File pdfFile, char delimiter) throws IOException {
        log.debug("Starting delimiter-separated to PDF conversion for file: {} (delimiter: '{}')",
                inputFile.getOriginalFilename(), delimiter == '\t' ? "\\t" : String.valueOf(delimiter));

        // Validate file size
        long fileSize = inputFile.getSize();
        if (fileSize > MAX_FILE_SIZE) {
            log.warn("File exceeds maximum size: {} bytes (max: {})", fileSize, MAX_FILE_SIZE);
            throw new IOException("File size exceeds maximum allowed: " + MAX_FILE_SIZE + " bytes");
        }

        // Route to streaming or in-memory processing based on file size
        if (fileSize > STREAMING_THRESHOLD) {
            log.debug("Using streaming mode for large file: {} bytes", fileSize);
            convertStreaming(inputFile, pdfFile, delimiter);
        } else {
            log.debug("Using in-memory mode for file: {} bytes", fileSize);
            convertInMemory(inputFile, pdfFile, delimiter);
        }
    }

    /**
     * Convert delimiter-separated file to PDF using in-memory processing for smaller files.
     */
    private void convertInMemory(MultipartFile inputFile, File pdfFile, char delimiter) throws IOException {
        List<String[]> rows = new ArrayList<>();
        int maxColumns = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputFile.getInputStream()))) {
            String line;
            int lineNumber = 0;

            while ((line = br.readLine()) != null) {
                lineNumber++;

                if (line.length() > MAX_LINE_LENGTH) {
                    log.warn("Line {} exceeds maximum length: {} chars (max: {})",
                            lineNumber, line.length(), MAX_LINE_LENGTH);
                    throw new IOException("Line " + lineNumber + " exceeds maximum length: " + MAX_LINE_LENGTH);
                }

                String[] values = parseLine(line, lineNumber, delimiter);

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
            log.warn("File is empty: {}", inputFile.getOriginalFilename());
            throw new IOException("File is empty");
        }

        log.debug("Parsed {} rows with max {} columns", rows.size(), maxColumns);

        String[][] tableData = normalizeRows(rows, maxColumns);

        try (PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
            builder.addTable(tableData);
            builder.save(pdfFile);
            log.info("Successfully converted to PDF: {} -> {}", inputFile.getOriginalFilename(), pdfFile.getName());
        } catch (Exception e) {
            log.error("Error creating PDF: {}", e.getMessage(), e);
            throw new IOException("Error creating PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Convert delimiter-separated file to PDF using streaming for large files.
     */
    private void convertStreaming(MultipartFile inputFile, File pdfFile, char delimiter) throws IOException {
        try (PdfDocumentBuilder builder = pdfBackend.createBuilder();
             BufferedReader br = new BufferedReader(new InputStreamReader(inputFile.getInputStream()))) {

            List<String[]> chunk = new ArrayList<>();
            String line;
            int maxColumns = 0;
            int lineNumber = 0;
            int totalRows = 0;

            while ((line = br.readLine()) != null) {
                lineNumber++;

                if (line.length() > MAX_LINE_LENGTH) {
                    log.warn("Line {} exceeds maximum length: {} chars (max: {})",
                            lineNumber, line.length(), MAX_LINE_LENGTH);
                    throw new IOException("Line " + lineNumber + " exceeds maximum length: " + MAX_LINE_LENGTH);
                }

                String[] values = parseLine(line, lineNumber, delimiter);

                if (values.length > MAX_FIELDS) {
                    log.warn("Line {} exceeds maximum field count: {} fields (max: {})",
                            lineNumber, values.length, MAX_FIELDS);
                    throw new IOException("Line " + lineNumber + " exceeds maximum field count: " + MAX_FIELDS);
                }

                chunk.add(values);
                maxColumns = Math.max(maxColumns, values.length);

                if (chunk.size() >= CHUNK_SIZE) {
                    String[][] tableData = normalizeRows(chunk, maxColumns);
                    builder.addTable(tableData);
                    totalRows += chunk.size();
                    log.debug("Processed chunk of {} rows (total: {})", chunk.size(), totalRows);
                    chunk.clear();
                }
            }

            if (!chunk.isEmpty()) {
                String[][] tableData = normalizeRows(chunk, maxColumns);
                builder.addTable(tableData);
                totalRows += chunk.size();
                log.debug("Processed final chunk of {} rows (total: {})", chunk.size(), totalRows);
            }

            if (totalRows == 0) {
                log.warn("File is empty: {}", inputFile.getOriginalFilename());
                throw new IOException("File is empty");
            }

            builder.save(pdfFile);
            log.info("Successfully converted to PDF using streaming: {} -> {} ({} rows)",
                    inputFile.getOriginalFilename(), pdfFile.getName(), totalRows);

        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating PDF: {}", e.getMessage(), e);
            throw new IOException("Error creating PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Normalize rows to have the same number of columns.
     */
    private String[][] normalizeRows(List<String[]> rows, int maxColumns) {
        String[][] tableData = new String[rows.size()][maxColumns];
        for (int i = 0; i < rows.size(); i++) {
            String[] row = rows.get(i);
            for (int j = 0; j < maxColumns; j++) {
                tableData[i][j] = j < row.length ? row[j] : "";
            }
        }
        return tableData;
    }

    /**
     * Parse a delimiter-separated line handling quoted values and escaped quotes.
     */
    String[] parseLine(String line, int lineNumber, char delimiter) {
        List<String> values = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    currentValue.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == delimiter && !inQuotes) {
                values.add(currentValue.toString());
                currentValue = new StringBuilder();
            } else {
                currentValue.append(c);
            }
        }

        if (inQuotes) {
            log.warn("Unclosed quote in line {}, treating as literal", lineNumber);
        }

        values.add(currentValue.toString());

        return values.toArray(new String[0]);
    }
}
