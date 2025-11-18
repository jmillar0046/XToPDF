package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests using actual binary test files from resources
 */
class ComprehensiveBinaryFileTests {

    @TempDir
    Path tempDir;

    @Test
    void testPptxToPdfService_RealFile() throws Exception {
        PptxToPdfService service = new PptxToPdfService();
        
        ClassPathResource resource = new ClassPathResource("test-files/test.pptx");
        try (InputStream is = resource.getInputStream()) {
            byte[] fileBytes = is.readAllBytes();
            MockMultipartFile file = new MockMultipartFile("test.pptx", "test.pptx",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation", fileBytes);
            
            File output = tempDir.resolve("output.pdf").toFile();
            service.convertPptxToPdf(file, output);
            
            assertTrue(output.exists());
            assertTrue(output.length() > 0);
        }
    }

    @Test
    void testXlsxToPdfService_RealFile() throws Exception {
        XlsxToPdfService service = new XlsxToPdfService();
        
        ClassPathResource resource = new ClassPathResource("test-files/test.xlsx");
        try (InputStream is = resource.getInputStream()) {
            byte[] fileBytes = is.readAllBytes();
            MockMultipartFile file = new MockMultipartFile("test.xlsx", "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", fileBytes);
            
            File output = tempDir.resolve("output.pdf").toFile();
            service.convertXlsxToPdf(file, output, false);
            
            assertTrue(output.exists());
            assertTrue(output.length() > 0);
        }
    }

    @Test
    void testXlsxToPdfService_WithExecuteMacros() throws Exception {
        XlsxToPdfService service = new XlsxToPdfService();
        
        ClassPathResource resource = new ClassPathResource("test-files/test.xlsx");
        try (InputStream is = resource.getInputStream()) {
            byte[] fileBytes = is.readAllBytes();
            MockMultipartFile file = new MockMultipartFile("test.xlsx", "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", fileBytes);
            
            File output = tempDir.resolve("output_macros.pdf").toFile();
            service.convertXlsxToPdf(file, output, true);
            
            assertTrue(output.exists());
            assertTrue(output.length() > 0);
        }
    }

    @Test
    void testXlsToPdfService_RealFile() throws Exception {
        XlsToPdfService service = new XlsToPdfService();
        
        ClassPathResource resource = new ClassPathResource("test-files/test.xls");
        try (InputStream is = resource.getInputStream()) {
            byte[] fileBytes = is.readAllBytes();
            MockMultipartFile file = new MockMultipartFile("test.xls", "test.xls",
                "application/vnd.ms-excel", fileBytes);
            
            File output = tempDir.resolve("output.pdf").toFile();
            service.convertXlsToPdf(file, output, false);
            
            assertTrue(output.exists());
            assertTrue(output.length() > 0);
        }
    }

    @Test
    void testXlsToPdfService_WithMacros() throws Exception {
        XlsToPdfService service = new XlsToPdfService();
        
        ClassPathResource resource = new ClassPathResource("test-files/test.xls");
        try (InputStream is = resource.getInputStream()) {
            byte[] fileBytes = is.readAllBytes();
            MockMultipartFile file = new MockMultipartFile("test.xls", "test.xls",
                "application/vnd.ms-excel", fileBytes);
            
            File output = tempDir.resolve("output_macros.pdf").toFile();
            service.convertXlsToPdf(file, output, true);
            
            assertTrue(output.exists());
            assertTrue(output.length() > 0);
        }
    }

    @Test
    void testDocxToPdfService_RealFile() throws Exception {
        DocxToPdfService service = new DocxToPdfService();
        
        ClassPathResource resource = new ClassPathResource("test-files/test.docx");
        try (InputStream is = resource.getInputStream()) {
            byte[] fileBytes = is.readAllBytes();
            MockMultipartFile file = new MockMultipartFile("test.docx", "test.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", fileBytes);
            
            File output = tempDir.resolve("output.pdf").toFile();
            service.convertDocxToPdf(file, output);
            
            assertTrue(output.exists());
            assertTrue(output.length() > 0);
        }
    }

    @Test
    void testPptToPdfService_RealFile() throws Exception {
        PptToPdfService service = new PptToPdfService();
        
        ClassPathResource resource = new ClassPathResource("test-files/test.ppt");
        try (InputStream is = resource.getInputStream()) {
            byte[] fileBytes = is.readAllBytes();
            MockMultipartFile file = new MockMultipartFile("test.ppt", "test.ppt",
                "application/vnd.ms-powerpoint", fileBytes);
            
            File output = tempDir.resolve("output.pdf").toFile();
            service.convertPptToPdf(file, output);
            
            assertTrue(output.exists());
            assertTrue(output.length() > 0);
        }
    }

    @Test
    void testBmpToPdfService_RealFile() throws Exception {
        BmpToPdfService service = new BmpToPdfService();
        
        ClassPathResource resource = new ClassPathResource("test-files/test.bmp");
        try (InputStream is = resource.getInputStream()) {
            byte[] fileBytes = is.readAllBytes();
            MockMultipartFile file = new MockMultipartFile("test.bmp", "test.bmp",
                "image/bmp", fileBytes);
            
            File output = tempDir.resolve("output.pdf").toFile();
            service.convertBmpToPdf(file, output);
            
            assertTrue(output.exists());
            assertTrue(output.length() > 0);
        }
    }

    @Test
    void testOdtToPdfService_RealFile() throws Exception {
        OdtToPdfService service = new OdtToPdfService();
        
        ClassPathResource resource = new ClassPathResource("test-files/test.odt");
        try (InputStream is = resource.getInputStream()) {
            byte[] fileBytes = is.readAllBytes();
            MockMultipartFile file = new MockMultipartFile("test.odt", "test.odt",
                "application/vnd.oasis.opendocument.text", fileBytes);
            
            File output = tempDir.resolve("output.pdf").toFile();
            service.convertOdtToPdf(file, output);
            
            assertTrue(output.exists());
            assertTrue(output.length() > 0);
        }
    }

    @Test
    void testOdsToPdfService_RealFile() throws Exception {
        OdsToPdfService service = new OdsToPdfService();
        
        ClassPathResource resource = new ClassPathResource("test-files/test.ods");
        try (InputStream is = resource.getInputStream()) {
            byte[] fileBytes = is.readAllBytes();
            MockMultipartFile file = new MockMultipartFile("test.ods", "test.ods",
                "application/vnd.oasis.opendocument.spreadsheet", fileBytes);
            
            File output = tempDir.resolve("output.pdf").toFile();
            service.convertOdsToPdf(file, output);
            
            assertTrue(output.exists());
            assertTrue(output.length() > 0);
        }
    }

    @Test
    void testOdpToPdfService_RealFile() throws Exception {
        OdpToPdfService service = new OdpToPdfService();
        
        ClassPathResource resource = new ClassPathResource("test-files/test.odp");
        try (InputStream is = resource.getInputStream()) {
            byte[] fileBytes = is.readAllBytes();
            MockMultipartFile file = new MockMultipartFile("test.odp", "test.odp",
                "application/vnd.oasis.opendocument.presentation", fileBytes);
            
            File output = tempDir.resolve("output.pdf").toFile();
            service.convertOdpToPdf(file, output);
            
            assertTrue(output.exists());
            assertTrue(output.length() > 0);
        }
    }
}
