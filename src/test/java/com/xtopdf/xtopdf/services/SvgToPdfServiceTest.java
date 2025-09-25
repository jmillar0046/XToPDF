package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class SvgToPdfServiceTest {

    @Test
    void testConvertValidSvgToPdf() throws Exception {
        SvgToPdfService service = new SvgToPdfService();
        String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100\" height=\"100\">" +
                     "<circle cx=\"50\" cy=\"50\" r=\"40\" stroke=\"black\" stroke-width=\"3\" fill=\"red\"/>" +
                     "</svg>";
        MockMultipartFile svgFile = new MockMultipartFile("file", "test.svg", "image/svg+xml", svg.getBytes());
        File pdfFile = new File(System.getProperty("java.io.tmpdir") + "/test.pdf");
        
        service.convertSvgToPdf(svgFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
        
        // Clean up
        pdfFile.delete();
    }

    @Test
    void testConvertEmptySvgToPdf() throws Exception {
        SvgToPdfService service = new SvgToPdfService();
        MockMultipartFile svgFile = new MockMultipartFile("file", "empty.svg", "image/svg+xml", new byte[0]);
        File pdfFile = new File(System.getProperty("java.io.tmpdir") + "/empty.pdf");
        
        assertThrows(Exception.class, () -> service.convertSvgToPdf(svgFile, pdfFile));
        
        // Clean up if file was created
        if (pdfFile.exists()) {
            pdfFile.delete();
        }
    }

    @Test
    void testConvertInvalidSvgDoesNotThrow() {
        SvgToPdfService service = new SvgToPdfService();
        MockMultipartFile svgFile = new MockMultipartFile("file", "invalid.svg", "image/svg+xml", "<svg><invalid>".getBytes());
        File pdfFile = new File(System.getProperty("java.io.tmpdir") + "/invalid.pdf");
        
        // Invalid SVG should still create a PDF file (iText is tolerant)
        assertDoesNotThrow(() -> service.convertSvgToPdf(svgFile, pdfFile));
        assertTrue(pdfFile.exists());
        
        // Clean up
        pdfFile.delete();
    }

    @Test
    void testConvertSvgToPdfWithNullFileThrows() {
        SvgToPdfService service = new SvgToPdfService();
        File pdfFile = new File(System.getProperty("java.io.tmpdir") + "/nullfile.pdf");
        
        assertThrows(Exception.class, () -> service.convertSvgToPdf(null, pdfFile));
        
        // Clean up if file was created
        if (pdfFile.exists()) {
            pdfFile.delete();
        }
    }

    @Test
    void testConvertSvgToPdfWithNullOutputFileThrows() {
        SvgToPdfService service = new SvgToPdfService();
        String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100\" height=\"100\"></svg>";
        MockMultipartFile svgFile = new MockMultipartFile("file", "test.svg", "image/svg+xml", svg.getBytes());
        
        assertThrows(Exception.class, () -> service.convertSvgToPdf(svgFile, null));
    }

    @Test
    void testConvertSvgToPdfWithInvalidOutputPath() {
        SvgToPdfService service = new SvgToPdfService();
        String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100\" height=\"100\"></svg>";
        MockMultipartFile svgFile = new MockMultipartFile("file", "test.svg", "image/svg+xml", svg.getBytes());
        File pdfFile = new File("/nonexistent/path/test.pdf");
        
        assertThrows(Exception.class, () -> service.convertSvgToPdf(svgFile, pdfFile));
    }
}