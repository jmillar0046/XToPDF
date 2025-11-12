package com.xtopdf.xtopdf.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Helper utility for common PDF file operations in controllers.
 * Handles temporary file creation, cleanup, and response building.
 */
@Slf4j
public class PdfFileHelper {

    /**
     * Functional interface for PDF processing operations that may throw exceptions.
     */
    @FunctionalInterface
    public interface PdfProcessor {
        void process(File file) throws Exception;
    }

    /**
     * Processes a PDF file with automatic temp file management and error handling.
     * 
     * @param pdfFile The uploaded PDF file
     * @param processor The processing function that operates on the temp file
     * @param outputFilename The filename for the response attachment
     * @return ResponseEntity with the processed PDF bytes or error status
     */
    public static ResponseEntity<byte[]> processPdfFile(
            MultipartFile pdfFile,
            PdfProcessor processor,
            String outputFilename) {
        
        File tempPdf = null;
        
        try {
            // Create temporary file for the PDF
            tempPdf = Files.createTempFile("pdf_", ".pdf").toFile();
            
            // Save uploaded file to temp file
            try (FileOutputStream fos = new FileOutputStream(tempPdf)) {
                fos.write(pdfFile.getBytes());
            }
            
            // Process the PDF
            processor.process(tempPdf);
            
            // Read the modified PDF
            byte[] modifiedPdfBytes = Files.readAllBytes(tempPdf.toPath());
            
            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "attachment; filename=\"" + outputFilename + "\"")
                    .body(modifiedPdfBytes);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid parameter: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (IOException e) {
            log.error("Error processing PDF: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Error processing PDF: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            // Clean up temporary file
            if (tempPdf != null && tempPdf.exists()) {
                tempPdf.delete();
            }
        }
    }

    /**
     * Writes a MultipartFile to a temporary file.
     * 
     * @param multipartFile The file to write
     * @param prefix Prefix for temp file name
     * @return The created temporary File
     * @throws IOException if file operations fail
     */
    public static File writeToTempFile(MultipartFile multipartFile, String prefix) throws IOException {
        File tempFile = Files.createTempFile(prefix, ".pdf").toFile();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(multipartFile.getBytes());
        }
        return tempFile;
    }
}
