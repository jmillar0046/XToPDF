package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DxfToPdfServiceTest {

    private DxfToPdfService dxfToPdfService;
    private File pdfFile;

    @BeforeEach
    void setUp() {
        dxfToPdfService = new DxfToPdfService();
    }

    @Test
    void testConvertDxfToPdf_Success() throws Exception {
        // Create a simple DXF content (basic DXF header)
        var content = "0\nSECTION\n2\nHEADER\n0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "test.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/testDxfOutput.pdf");

        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);

        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
        
        // Clean up
        pdfFile.delete();
    }

    @Test
    void testConvertDxfToPdf_EmptyFile() throws Exception {
        var content = "";
        var dxfFile = new MockMultipartFile("file", "test.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/testEmptyDxfOutput.pdf");

        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);

        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        
        // Clean up
        pdfFile.delete();
    }

    @Test
    void testConvertDxfToPdf_InvalidPdfCreation() {
        var content = "Test DXF content";
        var dxfFile = new MockMultipartFile("file", "test.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        assertThrows(IOException.class, () -> {
            dxfToPdfService.convertDxfToPdf(dxfFile, null);
        });
    }

    @Test
    void testConvertDxfToPdf_WithComplexContent() throws Exception {
        var content = "0\nSECTION\n2\nHEADER\n9\n$ACADVER\n1\nAC1009\n0\nENDSEC\n0\nSECTION\n2\nENTITIES\n0\nLINE\n8\n0\n10\n0.0\n20\n0.0\n11\n100.0\n21\n100.0\n0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "complex.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/complexDxfOutput.pdf");
        
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
        
        // Clean up
        pdfFile.delete();
    }

    @Test
    void testConvertDxfToPdf_NullMultipartFile_ThrowsNullPointerException() {
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/nullInput.pdf");
        assertThrows(NullPointerException.class, () -> dxfToPdfService.convertDxfToPdf(null, pdfFile));
    }

    @Test
    void testConvertDxfToPdf_NullOutputFile_ThrowsIOException() {
        var content = "test";
        var dxfFile = new MockMultipartFile("file", "test.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        assertThrows(IOException.class, () -> dxfToPdfService.convertDxfToPdf(dxfFile, null));
    }

    @Test
    void testConvertDxfToPdf_MultipleLines() throws Exception {
        var content = "Line 1\nLine 2\nLine 3";
        var dxfFile = new MockMultipartFile("file", "multilines.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/multilinesDxfOutput.pdf");
        
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
        
        // Clean up
        pdfFile.delete();
    }

    @Test
    void testConvertDxfToPdf_WithAllEntityTypes() throws Exception {
        // Create comprehensive DXF with multiple entity types
        var content = "0\nSECTION\n2\nHEADER\n9\n$ACADVER\n1\nAC1009\n0\nENDSEC\n0\nSECTION\n2\nENTITIES\n" +
                      "0\nLINE\n8\n0\n10\n0.0\n20\n0.0\n11\n100.0\n21\n100.0\n" +
                      "0\nCIRCLE\n8\n0\n10\n50.0\n20\n50.0\n40\n25.0\n" +
                      "0\nARC\n8\n0\n10\n0.0\n20\n0.0\n40\n10.0\n50\n0.0\n51\n90.0\n" +
                      "0\nPOINT\n8\n0\n10\n10.0\n20\n20.0\n" +
                      "0\nTEXT\n8\n0\n1\nHello\n10\n10.0\n20\n10.0\n40\n5.0\n" +
                      "0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "all_entities.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/allEntitiesDxfOutput.pdf");
        
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
        
        // Clean up
        pdfFile.delete();
    }

    @Test
    void testConvertDxfToPdf_WithBlocks() throws Exception {
        var content = "0\nSECTION\n2\nHEADER\n9\n$ACADVER\n1\nAC1009\n0\nENDSEC\n" +
                      "0\nSECTION\n2\nBLOCKS\n" +
                      "0\nBLOCK\n2\nTestBlock\n10\n0.0\n20\n0.0\n" +
                      "0\nLINE\n8\n0\n10\n0.0\n20\n0.0\n11\n10.0\n21\n10.0\n" +
                      "0\nENDBLK\n" +
                      "0\nENDSEC\n" +
                      "0\nSECTION\n2\nENTITIES\n" +
                      "0\nINSERT\n2\nTestBlock\n10\n50.0\n20\n50.0\n" +
                      "0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "blocks.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/blocksDxfOutput.pdf");
        
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        
        // Clean up
        pdfFile.delete();
    }

    @Test
    void testConvertDxfToPdf_With3DEntities() throws Exception {
        var content = "0\nSECTION\n2\nHEADER\n0\nENDSEC\n0\nSECTION\n2\nENTITIES\n" +
                      "0\n3DFACE\n8\n0\n10\n0.0\n20\n0.0\n30\n0.0\n11\n10.0\n21\n0.0\n31\n0.0\n12\n10.0\n22\n10.0\n32\n0.0\n13\n0.0\n23\n10.0\n33\n0.0\n" +
                      "0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "3d.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/3dDxfOutput.pdf");
        
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        
        // Clean up
        pdfFile.delete();
    }
}
