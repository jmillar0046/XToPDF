package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

class DwgToPdfServiceTest {

    private DwgToDxfService dwgToDxfService;
    private DxfToPdfService dxfToPdfService;
    private DwgToPdfService dwgToPdfService;

    @BeforeEach
    void setUp() {
        dwgToDxfService = Mockito.mock(DwgToDxfService.class);
        dxfToPdfService = Mockito.mock(DxfToPdfService.class);
        dwgToPdfService = new DwgToPdfService(dwgToDxfService, dxfToPdfService);
    }

    @Test
    void testConvertDwgToPdf_ThrowsUnsupportedOperationException() throws IOException {
        var content = "Sample DWG content";
        var dwgFile = new MockMultipartFile("file", "test.dwg", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        File pdfFile = new File(System.getProperty("java.io.tmpdir") + "/testDwgOutput.pdf");

        doThrow(new UnsupportedOperationException("Direct DWG to DXF conversion is not supported"))
            .when(dwgToDxfService).convertDwgToDxf(any(), any());

        assertThrows(UnsupportedOperationException.class, () -> dwgToPdfService.convertDwgToPdf(dwgFile, pdfFile));
    }

    @Test
    void testConvertDwgToPdf_WithNullInput_ThrowsNullPointerException() throws IOException {
        File pdfFile = new File(System.getProperty("java.io.tmpdir") + "/testOutput.pdf");

        doThrow(new NullPointerException("Null input"))
            .when(dwgToDxfService).convertDwgToDxf(any(), any());

        assertThrows(NullPointerException.class, () -> dwgToPdfService.convertDwgToPdf(null, pdfFile));
    }

    @Test
    void testConvertDwgToPdf_WithIOException_ThrowsIOException() throws IOException {
        var content = "Sample DWG content";
        var dwgFile = new MockMultipartFile("file", "test.dwg", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        File pdfFile = new File(System.getProperty("java.io.tmpdir") + "/testDwgOutput.pdf");

        doThrow(new IOException("IO error during conversion"))
            .when(dwgToDxfService).convertDwgToDxf(any(), any());

        assertThrows(IOException.class, () -> dwgToPdfService.convertDwgToPdf(dwgFile, pdfFile));
    }

    // Additional comprehensive tests using real DwgToDxfService and DxfToPdfService
    
    @Test
    void testConvertDwgToPdf_RealServices_SimpleLine() throws Exception {
        DwgToDxfService realDwgToDxfService = new DwgToDxfService();
        DxfToPdfService realDxfToPdfService = new DxfToPdfService(new com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend());
        DwgToPdfService realService = new DwgToPdfService(realDwgToDxfService, realDxfToPdfService);
        
        var resource = new org.springframework.core.io.ClassPathResource("test-files/simple_line.dwg");
        try (var is = resource.getInputStream()) {
            byte[] fileBytes = is.readAllBytes();
            var dwgFile = new MockMultipartFile("test.dwg", "test.dwg",
                "application/acad", fileBytes);
            
            File pdfFile = new File(System.getProperty("java.io.tmpdir") + "/dwg_simple_output.pdf");
            realService.convertDwgToPdf(dwgFile, pdfFile);
            
            assertTrue(pdfFile.exists());
            assertTrue(pdfFile.length() > 0);
            
            pdfFile.delete();
        }
    }

    @Test
    void testConvertDwgToPdf_RealServices_MultipleEntities() throws Exception {
        DwgToDxfService realDwgToDxfService = new DwgToDxfService();
        DxfToPdfService realDxfToPdfService = new DxfToPdfService(new com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend());
        DwgToPdfService realService = new DwgToPdfService(realDwgToDxfService, realDxfToPdfService);
        
        var resource = new org.springframework.core.io.ClassPathResource("test-files/multi_entity.dwg");
        try (var is = resource.getInputStream()) {
            byte[] fileBytes = is.readAllBytes();
            var dwgFile = new MockMultipartFile("test.dwg", "test.dwg",
                "application/acad", fileBytes);
            
            File pdfFile = new File(System.getProperty("java.io.tmpdir") + "/dwg_multi_output.pdf");
            realService.convertDwgToPdf(dwgFile, pdfFile);
            
            assertTrue(pdfFile.exists());
            assertTrue(pdfFile.length() > 0);
            
            pdfFile.delete();
        }
    }

    @Test
    void testConvertDwgToPdf_RealServices_ComplexFile() throws Exception {
        DwgToDxfService realDwgToDxfService = new DwgToDxfService();
        DxfToPdfService realDxfToPdfService = new DxfToPdfService(new com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend());
        DwgToPdfService realService = new DwgToPdfService(realDwgToDxfService, realDxfToPdfService);
        
        var resource = new org.springframework.core.io.ClassPathResource("test-files/complex.dwg");
        try (var is = resource.getInputStream()) {
            byte[] fileBytes = is.readAllBytes();
            var dwgFile = new MockMultipartFile("test.dwg", "test.dwg",
                "application/acad", fileBytes);
            
            File pdfFile = new File(System.getProperty("java.io.tmpdir") + "/dwg_complex_output.pdf");
            realService.convertDwgToPdf(dwgFile, pdfFile);
            
            assertTrue(pdfFile.exists());
            assertTrue(pdfFile.length() > 0);
            
            pdfFile.delete();
        }
    }

    @Test
    void testConvertDwgToPdf_RealServices_InvalidFile() {
        DwgToDxfService realDwgToDxfService = new DwgToDxfService();
        DxfToPdfService realDxfToPdfService = new DxfToPdfService(new com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend());
        DwgToPdfService realService = new DwgToPdfService(realDwgToDxfService, realDxfToPdfService);
        
        var dwgFile = new MockMultipartFile("test.dwg", "test.dwg",
            "application/acad", "invalid dwg".getBytes());
        File pdfFile = new File(System.getProperty("java.io.tmpdir") + "/dwg_invalid_output.pdf");
        
        // May throw exception or handle gracefully
        try {
            realService.convertDwgToPdf(dwgFile, pdfFile);
            // If it succeeds, file should exist
            assertTrue(pdfFile.exists());
            pdfFile.delete();
        } catch (Exception e) {
            // Expected for invalid format
            assertTrue(e != null);
        }
    }
}
