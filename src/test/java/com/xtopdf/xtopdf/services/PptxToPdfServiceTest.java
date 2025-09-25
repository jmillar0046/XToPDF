package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PptxToPdfServiceTest {

    private PptxToPdfService pptxToPdfService;
    private File pdfFile;

    @BeforeEach
    void setUp() {
        pptxToPdfService = new PptxToPdfService();
    }

    @Test
    void testConvertPptxToPdf_InvalidPptxContent_ThrowsIOException() {
        // Create a mock file with invalid PPTX content
        var invalidContent = "This is not a valid PPTX file content";
        var pptxFile = new MockMultipartFile("file", "test.pptx", 
            "application/vnd.openxmlformats-officedocument.presentationml.presentation", 
            invalidContent.getBytes());

        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/testOutput.pdf");

        assertThrows(IOException.class, () -> pptxToPdfService.convertPptxToPdf(pptxFile, pdfFile));
    }

    @Test
    void testConvertPptxToPdf_NullMultipartFile_ThrowsNullPointerException() {
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/nullInput.pdf");
        assertThrows(NullPointerException.class, () -> pptxToPdfService.convertPptxToPdf(null, pdfFile));
    }

    @Test
    void testConvertPptxToPdf_NullOutputFile_ThrowsIOException() {
        var content = "fake pptx content";
        var pptxFile = new MockMultipartFile("file", "test.pptx", 
            "application/vnd.openxmlformats-officedocument.presentationml.presentation", 
            content.getBytes());
        
        assertThrows(IOException.class, () -> pptxToPdfService.convertPptxToPdf(pptxFile, null));
    }

    @Test
    void testConvertPptxToPdf_EmptyFile_ThrowsIOException() {
        var emptyContent = new byte[0];
        var pptxFile = new MockMultipartFile("file", "empty.pptx", 
            "application/vnd.openxmlformats-officedocument.presentationml.presentation", 
            emptyContent);

        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/emptyOutput.pdf");

        assertThrows(IOException.class, () -> pptxToPdfService.convertPptxToPdf(pptxFile, pdfFile));
    }

    @Test
    void testConvertPptxToPdf_InvalidOutputPath_ThrowsIOException() {
        var content = "fake pptx content";
        var pptxFile = new MockMultipartFile("file", "test.pptx", 
            "application/vnd.openxmlformats-officedocument.presentationml.presentation", 
            content.getBytes());

        // Use an invalid path (directory doesn't exist)
        pdfFile = new File("/invalid/path/that/does/not/exist/output.pdf");

        assertThrows(IOException.class, () -> pptxToPdfService.convertPptxToPdf(pptxFile, pdfFile));
    }
}