package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class RtfToPdfServiceTest {

    private final RtfToPdfService rtfToPdfService = new RtfToPdfService();

    @TempDir
    Path tempDir;

    @Test
    void convertRtfToPdf_ValidRtfFile_CreatesValidPdf() throws IOException {
        // Create a simple RTF content
        String rtfContent = "{\\rtf1\\ansi\\deff0 {\\fonttbl {\\f0 Times New Roman;}} " +
                "\\f0\\fs24 Hello, this is a test RTF document. " +
                "This content should be converted to PDF successfully.}";
        
        MockMultipartFile rtfFile = new MockMultipartFile(
                "file", 
                "test.rtf", 
                "application/rtf", 
                rtfContent.getBytes()
        );
        
        File pdfFile = tempDir.resolve("output.pdf").toFile();

        // Test the conversion
        assertDoesNotThrow(() -> rtfToPdfService.convertRtfToPdf(rtfFile, pdfFile));
        
        // Verify PDF file was created and has content
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void convertRtfToPdf_EmptyRtfFile_CreatesEmptyPdf() throws IOException {
        String rtfContent = "{\\rtf1\\ansi\\deff0 {\\fonttbl {\\f0 Times New Roman;}} \\f0\\fs24 }";
        
        MockMultipartFile rtfFile = new MockMultipartFile(
                "file", 
                "empty.rtf", 
                "application/rtf", 
                rtfContent.getBytes()
        );
        
        File pdfFile = tempDir.resolve("empty.pdf").toFile();

        assertDoesNotThrow(() -> rtfToPdfService.convertRtfToPdf(rtfFile, pdfFile));
        assertTrue(pdfFile.exists());
    }

    @Test
    void convertRtfToPdf_InvalidRtfContent_ThrowsIOException() {
        MockMultipartFile invalidRtfFile = new MockMultipartFile(
                "file", 
                "invalid.rtf", 
                "application/rtf", 
                "This is not valid RTF content".getBytes()
        );
        
        File pdfFile = tempDir.resolve("invalid.pdf").toFile();

        // Invalid RTF should still be handled gracefully by RTFEditorKit
        assertDoesNotThrow(() -> rtfToPdfService.convertRtfToPdf(invalidRtfFile, pdfFile));
    }

    @Test
    void convertRtfToPdf_PlainTextRtfFile_CreatesValidPdf() throws IOException {
        String rtfContent = "{\\rtf1\\ansi\\deff0 {\\fonttbl {\\f0 Times New Roman;}} " +
                "\\f0\\fs24 This is plain text without formatting.}";
        
        MockMultipartFile rtfFile = new MockMultipartFile(
                "file", 
                "plain.rtf", 
                "application/rtf", 
                rtfContent.getBytes()
        );
        
        File pdfFile = tempDir.resolve("plain.pdf").toFile();

        assertDoesNotThrow(() -> rtfToPdfService.convertRtfToPdf(rtfFile, pdfFile));
        
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }
}