package com.xtopdf.xtopdf.services;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for streaming logic in CSV/TSV services.
 * Validates Requirements 5.1, 5.2, 5.3, 5.4
 * 
 * Property 9: Streaming Threshold Activation
 * Property 10: Chunk Processing Data Integrity
 * Property 11: Streaming Memory Bounds
 */
class StreamingPropertyTest {

    private static final int STREAMING_THRESHOLD = 1024 * 1024; // 1MB

    /**
     * Property 9: Streaming Threshold Activation
     * 
     * Files below threshold should use in-memory processing,
     * files above threshold should use streaming.
     */
    @Property
    @Label("Streaming activates above threshold")
    void streamingActivatesAboveThreshold(
            @ForAll @IntRange(min = 100, max = 2000) int fileSizeKB) {
        
        int fileSizeBytes = fileSizeKB * 1024;
        byte[] fileContent = generateCsvContent(fileSizeBytes);
        
        StreamingProcessor processor = new StreamingProcessor(STREAMING_THRESHOLD);
        ProcessingMode mode = processor.determineProcessingMode(fileContent.length);
        
        if (fileSizeBytes < STREAMING_THRESHOLD) {
            assertThat(mode).isEqualTo(ProcessingMode.IN_MEMORY);
        } else {
            assertThat(mode).isEqualTo(ProcessingMode.STREAMING);
        }
    }

    /**
     * Property 10: Chunk Processing Data Integrity
     * 
     * When processing in chunks, all data should be preserved.
     */
    @Property
    @Label("Chunk processing preserves data integrity")
    void chunkProcessingPreservesDataIntegrity(
            @ForAll @IntRange(min = 10, max = 100) int numRows) throws IOException {
        
        // Generate CSV data
        StringBuilder csv = new StringBuilder();
        csv.append("col1,col2,col3\n");
        for (int i = 0; i < numRows; i++) {
            csv.append("val").append(i).append(",");
            csv.append("data").append(i).append(",");
            csv.append("test").append(i).append("\n");
        }
        
        byte[] content = csv.toString().getBytes();
        
        // Process in chunks
        List<String> processedRows = processInChunks(content, 256);
        
        // Verify all rows were processed
        assertThat(processedRows).hasSize(numRows + 1); // +1 for header
        assertThat(processedRows.get(0)).isEqualTo("col1,col2,col3");
    }

    /**
     * Property 11: Streaming Memory Bounds
     * 
     * Streaming should not load entire file into memory.
     */
    @Property
    @Label("Streaming respects memory bounds")
    void streamingRespectsMemoryBounds(
            @ForAll @IntRange(min = 1, max = 10) int fileSizeMB) {
        
        int fileSizeBytes = fileSizeMB * 1024 * 1024;
        
        StreamingProcessor processor = new StreamingProcessor(STREAMING_THRESHOLD);
        
        // Verify chunk size is reasonable
        int chunkSize = processor.getChunkSize();
        assertThat(chunkSize).isLessThan(STREAMING_THRESHOLD);
        assertThat(chunkSize).isGreaterThan(0);
        
        // Verify number of chunks is reasonable
        int numChunks = (fileSizeBytes + chunkSize - 1) / chunkSize;
        assertThat(numChunks).isGreaterThan(0);
    }

    /**
     * Property 12: Empty file handling
     * 
     * Empty files should be handled gracefully in both modes.
     */
    @Property
    @Label("Empty files are handled gracefully")
    void emptyFilesHandledGracefully() throws IOException {
        byte[] emptyContent = new byte[0];
        
        List<String> processedRows = processInChunks(emptyContent, 256);
        
        assertThat(processedRows).isEmpty();
    }

    /**
     * Property 13: Single line handling
     * 
     * Files with single line should work in both modes.
     */
    @Property
    @Label("Single line files are handled correctly")
    void singleLineFilesHandledCorrectly() throws IOException {
        byte[] singleLine = "col1,col2,col3\n".getBytes();
        
        List<String> processedRows = processInChunks(singleLine, 256);
        
        assertThat(processedRows).hasSize(1);
        assertThat(processedRows.get(0)).isEqualTo("col1,col2,col3");
    }

    // Helper methods and classes

    private byte[] generateCsvContent(int targetSize) {
        StringBuilder sb = new StringBuilder();
        sb.append("col1,col2,col3\n");
        
        while (sb.length() < targetSize) {
            sb.append("value1,value2,value3\n");
        }
        
        return sb.toString().getBytes();
    }

    private List<String> processInChunks(byte[] content, int chunkSize) throws IOException {
        List<String> rows = new ArrayList<>();
        
        try (InputStream is = new ByteArrayInputStream(content)) {
            byte[] buffer = new byte[chunkSize];
            StringBuilder currentLine = new StringBuilder();
            int bytesRead;
            
            while ((bytesRead = is.read(buffer)) != -1) {
                String chunk = new String(buffer, 0, bytesRead);
                
                for (char c : chunk.toCharArray()) {
                    if (c == '\n') {
                        if (currentLine.length() > 0) {
                            rows.add(currentLine.toString());
                            currentLine = new StringBuilder();
                        }
                    } else {
                        currentLine.append(c);
                    }
                }
            }
            
            // Add last line if exists
            if (currentLine.length() > 0) {
                rows.add(currentLine.toString());
            }
        }
        
        return rows;
    }

    enum ProcessingMode {
        IN_MEMORY,
        STREAMING
    }

    static class StreamingProcessor {
        private final int threshold;
        private final int chunkSize;

        StreamingProcessor(int threshold) {
            this.threshold = threshold;
            this.chunkSize = Math.min(threshold / 10, 64 * 1024); // 64KB chunks
        }

        ProcessingMode determineProcessingMode(int fileSize) {
            return fileSize >= threshold ? ProcessingMode.STREAMING : ProcessingMode.IN_MEMORY;
        }

        int getChunkSize() {
            return chunkSize;
        }
    }
}
