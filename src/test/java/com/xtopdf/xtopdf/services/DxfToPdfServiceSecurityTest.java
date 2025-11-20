package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Security-focused tests for DxfToPdfService, specifically testing the safe numeric cast methods.
 */
class DxfToPdfServiceSecurityTest {

    private DxfToPdfService dxfToPdfService;

    @BeforeEach
    void setUp() {
        dxfToPdfService = new DxfToPdfService(new com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend());
    }

    @Test
    void testDxfWithIntegerOverflow_DimensionType() throws Exception {
        // Test DXF with dimension type value that would overflow if not validated
        String dxf = "0\nSECTION\n2\nENTITIES\n" +
                     "0\nDIMENSION\n" +
                     "70\n" + ((long)Integer.MAX_VALUE + 1) + "\n" +
                     "0\nENDSEC\n0\nEOF\n";
        
        MockMultipartFile dxfFile = new MockMultipartFile("test.dxf", "test.dxf", "application/dxf", dxf.getBytes());
        File pdfFile = new File(System.getProperty("java.io.tmpdir") + "/test_overflow.pdf");
        
        try {
            // Should handle the overflow gracefully or throw appropriate exception
            dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        } catch (IOException e) {
            // Expected - value out of range should be caught
            assertTrue(e.getMessage().contains("Value out of int range") || 
                      e.getMessage().contains("Error") ||
                      e.getMessage().contains("Invalid"));
        } finally {
            if (pdfFile.exists()) pdfFile.delete();
        }
    }

    @Test
    void testDxfWithTableDimensions() throws Exception {
        // Test DXF with valid table dimensions
        String dxf = "0\nSECTION\n2\nENTITIES\n" +
                     "0\nACAD_TABLE\n" +
                     "90\n5\n" +  // rows
                     "91\n3\n" +  // columns
                     "0\nENDSEC\n0\nEOF\n";
        
        MockMultipartFile dxfFile = new MockMultipartFile("test.dxf", "test.dxf", "application/dxf", dxf.getBytes());
        File pdfFile = new File(System.getProperty("java.io.tmpdir") + "/test_table.pdf");
        
        try {
            dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
            assertTrue(pdfFile.exists());
        } finally {
            if (pdfFile.exists()) pdfFile.delete();
        }
    }

    @Test
    void testDxfWithMeshSubdivision() throws Exception {
        // Test DXF with mesh subdivision level
        String dxf = "0\nSECTION\n2\nENTITIES\n" +
                     "0\nMESH\n" +
                     "92\n2\n" +  // subdivision level
                     "0\nENDSEC\n0\nEOF\n";
        
        MockMultipartFile dxfFile = new MockMultipartFile("test.dxf", "test.dxf", "application/dxf", dxf.getBytes());
        File pdfFile = new File(System.getProperty("java.io.tmpdir") + "/test_mesh.pdf");
        
        try {
            dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
            assertTrue(pdfFile.exists());
        } finally {
            if (pdfFile.exists()) pdfFile.delete();
        }
    }

    @Test
    void testDxfWithSurfaceParameters() throws Exception {
        // Test DXF with surface U/V degree parameters
        String dxf = "0\nSECTION\n2\nENTITIES\n" +
                     "0\nSURFACE\n" +
                     "71\n3\n" +  // U degree
                     "72\n3\n" +  // V degree
                     "73\n10\n" + // num U control points
                     "74\n10\n" + // num V control points
                     "0\nENDSEC\n0\nEOF\n";
        
        MockMultipartFile dxfFile = new MockMultipartFile("test.dxf", "test.dxf", "application/dxf", dxf.getBytes());
        File pdfFile = new File(System.getProperty("java.io.tmpdir") + "/test_surface.pdf");
        
        try {
            dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
            assertTrue(pdfFile.exists());
        } finally {
            if (pdfFile.exists()) pdfFile.delete();
        }
    }

    @Test
    void testDxfWithBodyVersion() throws Exception {
        // Test DXF with body version
        String dxf = "0\nSECTION\n2\nENTITIES\n" +
                     "0\nBODY\n" +
                     "70\n1\n" +  // version
                     "0\nENDSEC\n0\nEOF\n";
        
        MockMultipartFile dxfFile = new MockMultipartFile("test.dxf", "test.dxf", "application/dxf", dxf.getBytes());
        File pdfFile = new File(System.getProperty("java.io.tmpdir") + "/test_body.pdf");
        
        try {
            dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
            assertTrue(pdfFile.exists());
        } finally {
            if (pdfFile.exists()) pdfFile.delete();
        }
    }

    @Test
    void testDxfWithInfiniteValue() throws Exception {
        // Test DXF with a value that would be infinite
        String dxf = "0\nSECTION\n2\nENTITIES\n" +
                     "0\nLINE\n" +
                     "10\n1.7976931348623157E308\n" + // Near Double.MAX_VALUE
                     "0\nENDSEC\n0\nEOF\n";
        
        MockMultipartFile dxfFile = new MockMultipartFile("test.dxf", "test.dxf", "application/dxf", dxf.getBytes());
        File pdfFile = new File(System.getProperty("java.io.tmpdir") + "/test_infinite.pdf");
        
        try {
            dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
            // Should either succeed or fail gracefully
            if (pdfFile.exists()) {
                assertTrue(pdfFile.length() > 0);
            }
        } catch (IOException e) {
            // Expected for invalid values
            assertTrue(e.getMessage().contains("Error") || e.getMessage().contains("Invalid"));
        } finally {
            if (pdfFile.exists()) pdfFile.delete();
        }
    }
}
