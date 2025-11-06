package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import static org.junit.jupiter.api.Assertions.*;

class PdfMergeServiceTest {

    private PdfMergeService pdfMergeService;
    private File tempDir;
    private File convertedPdf;
    private File existingPdfFile;

    @BeforeEach
    void setUp() throws IOException {
        pdfMergeService = new PdfMergeService();
        
        // Create temp directory for test files
        tempDir = Files.createTempDirectory("pdf-merge-test").toFile();
        
        // Create a converted PDF file
        convertedPdf = new File(tempDir, "converted.pdf");
        createTestPdf(convertedPdf, "Converted PDF Content");
        
        // Create an existing PDF file
        existingPdfFile = new File(tempDir, "existing.pdf");
        createTestPdf(existingPdfFile, "Existing PDF Content");
    }

    @AfterEach
    void tearDown() {
        // Clean up temp files
        if (tempDir != null && tempDir.exists()) {
            deleteDirectory(tempDir);
        }
    }

    @Test
    void testMergePdfsAtBack() throws IOException {
        // Create a multipart file from the existing PDF
        byte[] existingPdfBytes = Files.readAllBytes(existingPdfFile.toPath());
        MockMultipartFile existingPdfMultipart = new MockMultipartFile(
            "existingPdf", 
            "existing.pdf", 
            "application/pdf", 
            existingPdfBytes
        );

        // Get original size of converted PDF
        long originalSize = convertedPdf.length();

        // Merge PDFs with existing PDF at the back
        pdfMergeService.mergePdfs(convertedPdf, existingPdfMultipart, "back");

        // Verify that the merged file exists and is larger than the original
        assertTrue(convertedPdf.exists());
        assertTrue(convertedPdf.length() > originalSize);
    }

    @Test
    void testMergePdfsAtFront() throws IOException {
        // Create a multipart file from the existing PDF
        byte[] existingPdfBytes = Files.readAllBytes(existingPdfFile.toPath());
        MockMultipartFile existingPdfMultipart = new MockMultipartFile(
            "existingPdf", 
            "existing.pdf", 
            "application/pdf", 
            existingPdfBytes
        );

        // Get original size of converted PDF
        long originalSize = convertedPdf.length();

        // Merge PDFs with existing PDF at the front
        pdfMergeService.mergePdfs(convertedPdf, existingPdfMultipart, "front");

        // Verify that the merged file exists and is larger than the original
        assertTrue(convertedPdf.exists());
        assertTrue(convertedPdf.length() > originalSize);
    }

    @Test
    void testMergePdfsWithInvalidExistingPdf() {
        // Create a multipart file with invalid PDF content
        MockMultipartFile invalidPdfMultipart = new MockMultipartFile(
            "existingPdf", 
            "invalid.pdf", 
            "application/pdf", 
            "This is not a valid PDF".getBytes()
        );

        // Attempt to merge with invalid PDF should throw an IOException
        assertThrows(IOException.class, () -> {
            pdfMergeService.mergePdfs(convertedPdf, invalidPdfMultipart, "back");
        });
    }

    @Test
    void testMergePdfsWithCaseInsensitivePosition() throws IOException {
        // Create a multipart file from the existing PDF
        byte[] existingPdfBytes = Files.readAllBytes(existingPdfFile.toPath());
        MockMultipartFile existingPdfMultipart = new MockMultipartFile(
            "existingPdf", 
            "existing.pdf", 
            "application/pdf", 
            existingPdfBytes
        );

        // Test with uppercase BACK
        pdfMergeService.mergePdfs(convertedPdf, existingPdfMultipart, "BACK");
        assertTrue(convertedPdf.exists());

        // Recreate converted PDF for next test
        createTestPdf(convertedPdf, "Converted PDF Content");

        // Test with mixed case FrOnT
        pdfMergeService.mergePdfs(convertedPdf, existingPdfMultipart, "FrOnT");
        assertTrue(convertedPdf.exists());
    }

    @Test
    void testMergePdfsWithEmptyExistingPdf() {
        // Create an empty multipart file
        MockMultipartFile emptyPdfMultipart = new MockMultipartFile(
            "existingPdf", 
            "empty.pdf", 
            "application/pdf", 
            new byte[0]
        );

        // Attempt to merge with empty PDF should throw an IOException
        assertThrows(IOException.class, () -> {
            pdfMergeService.mergePdfs(convertedPdf, emptyPdfMultipart, "back");
        });
    }

    // Helper method to create a test PDF
    private void createTestPdf(File file, String content) throws IOException {
        try (PdfWriter writer = new PdfWriter(new FileOutputStream(file))) {
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);
            document.add(new Paragraph(content));
            document.close();
        }
    }

    // Helper method to delete directory recursively
    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
}
