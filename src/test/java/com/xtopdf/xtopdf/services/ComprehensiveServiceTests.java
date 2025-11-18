package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests to improve coverage across all service classes
 */
class ComprehensiveServiceTests {

    @Test
    void testPptxToPdfService_EmptyFile() {
        PptxToPdfService service = new PptxToPdfService();
        MockMultipartFile file = new MockMultipartFile("test.pptx", "test.pptx", 
            "application/vnd.openxmlformats-officedocument.presentationml.presentation", new byte[0]);
        File output = new File(System.getProperty("java.io.tmpdir") + "/test.pdf");
        
        assertThrows(IOException.class, () -> service.convertPptxToPdf(file, output));
    }

    @Test
    void testDocToPdfService_EmptyFile() {
        DocToPdfService service = new DocToPdfService();
        MockMultipartFile file = new MockMultipartFile("test.doc", "test.doc", 
            "application/msword", new byte[0]);
        File output = new File(System.getProperty("java.io.tmpdir") + "/test.pdf");
        
        assertThrows(IOException.class, () -> service.convertDocToPdf(file, output));
    }

    @Test
    void testBmpToPdfService_InvalidData() {
        BmpToPdfService service = new BmpToPdfService();
        MockMultipartFile file = new MockMultipartFile("test.bmp", "test.bmp", 
            "image/bmp", "invalid bmp data".getBytes());
        File output = new File(System.getProperty("java.io.tmpdir") + "/test.pdf");
        
        assertThrows(IOException.class, () -> service.convertBmpToPdf(file, output));
    }

    @Test
    void testXlsToPdfService_InvalidData() {
        XlsToPdfService service = new XlsToPdfService();
        MockMultipartFile file = new MockMultipartFile("test.xls", "test.xls", 
            "application/vnd.ms-excel", "invalid xls data".getBytes());
        File output = new File(System.getProperty("java.io.tmpdir") + "/test.pdf");
        
        assertThrows(IOException.class, () -> service.convertXlsToPdf(file, output, false));
    }

    @Test
    void testXlsToPdfService_WithMacros() {
        XlsToPdfService service = new XlsToPdfService();
        MockMultipartFile file = new MockMultipartFile("test.xls", "test.xls", 
            "application/vnd.ms-excel", "invalid xls data".getBytes());
        File output = new File(System.getProperty("java.io.tmpdir") + "/test.pdf");
        
        assertThrows(IOException.class, () -> service.convertXlsToPdf(file, output, true));
    }

    @Test
    void testXlsxToPdfService_InvalidData() {
        XlsxToPdfService service = new XlsxToPdfService();
        MockMultipartFile file = new MockMultipartFile("test.xlsx", "test.xlsx", 
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "invalid xlsx data".getBytes());
        File output = new File(System.getProperty("java.io.tmpdir") + "/test.pdf");
        
        assertThrows(IOException.class, () -> service.convertXlsxToPdf(file, output, false));
    }

    @Test
    void testXlsxToPdfService_WithMacros() {
        XlsxToPdfService service = new XlsxToPdfService();
        MockMultipartFile file = new MockMultipartFile("test.xlsx", "test.xlsx", 
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "invalid xlsx data".getBytes());
        File output = new File(System.getProperty("java.io.tmpdir") + "/test.pdf");
        
        assertThrows(IOException.class, () -> service.convertXlsxToPdf(file, output, true));
    }

    @Test
    void testOdsToPdfService_InvalidData() {
        OdsToPdfService service = new OdsToPdfService();
        MockMultipartFile file = new MockMultipartFile("test.ods", "test.ods", 
            "application/vnd.oasis.opendocument.spreadsheet", "invalid ods data".getBytes());
        File output = new File(System.getProperty("java.io.tmpdir") + "/test.pdf");
        
        assertThrows(IOException.class, () -> service.convertOdsToPdf(file, output));
    }

    @Test
    void testPptToPdfService_InvalidData() {
        PptToPdfService service = new PptToPdfService();
        MockMultipartFile file = new MockMultipartFile("test.ppt", "test.ppt", 
            "application/vnd.ms-powerpoint", "invalid ppt data".getBytes());
        File output = new File(System.getProperty("java.io.tmpdir") + "/test.pdf");
        
        assertThrows(IOException.class, () -> service.convertPptToPdf(file, output));
    }

    @Test
    void testOdpToPdfService_InvalidData() {
        OdpToPdfService service = new OdpToPdfService();
        MockMultipartFile file = new MockMultipartFile("test.odp", "test.odp", 
            "application/vnd.oasis.opendocument.presentation", "invalid odp data".getBytes());
        File output = new File(System.getProperty("java.io.tmpdir") + "/test.pdf");
        
        assertThrows(IOException.class, () -> service.convertOdpToPdf(file, output));
    }

    @Test
    void testOdtToPdfService_InvalidData() {
        OdtToPdfService service = new OdtToPdfService();
        MockMultipartFile file = new MockMultipartFile("test.odt", "test.odt", 
            "application/vnd.oasis.opendocument.text", "invalid odt data".getBytes());
        File output = new File(System.getProperty("java.io.tmpdir") + "/test.pdf");
        
        assertThrows(IOException.class, () -> service.convertOdtToPdf(file, output));
    }

}
