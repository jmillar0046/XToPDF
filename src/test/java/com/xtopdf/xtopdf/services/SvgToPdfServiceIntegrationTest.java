package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class SvgToPdfServiceIntegrationTest {

    @Test
    void testComplexSvgConversion() throws Exception {
        SvgToPdfService service = new SvgToPdfService();
        
        String complexSvg = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"200\" height=\"200\">\n" +
                "  <circle cx=\"100\" cy=\"100\" r=\"80\" stroke=\"black\" stroke-width=\"3\" fill=\"red\"/>\n" +
                "  <text x=\"100\" y=\"110\" text-anchor=\"middle\" font-family=\"Arial\" font-size=\"20\" fill=\"white\">SVG Test</text>\n" +
                "  <rect x=\"20\" y=\"20\" width=\"160\" height=\"30\" stroke=\"blue\" stroke-width=\"2\" fill=\"lightblue\"/>\n" +
                "  <text x=\"100\" y=\"40\" text-anchor=\"middle\" font-family=\"Arial\" font-size=\"16\" fill=\"black\">Hello PDF!</text>\n" +
                "</svg>";
        
        MockMultipartFile svgFile = new MockMultipartFile("file", "complex.svg", "image/svg+xml", complexSvg.getBytes());
        File pdfFile = new File(System.getProperty("java.io.tmpdir") + "/complex_test.pdf");
        
        service.convertSvgToPdf(svgFile, pdfFile);
        
        assertTrue(pdfFile.exists(), "PDF file should be created");
        assertTrue(pdfFile.length() > 1000, "PDF file should have reasonable size (> 1KB)");
        
        System.out.println("Complex SVG conversion successful!");
        System.out.println("Output file: " + pdfFile.getAbsolutePath());
        System.out.println("File size: " + pdfFile.length() + " bytes");
        
        // Clean up
        pdfFile.delete();
    }
    
    @Test
    void testSvgWithPathsConversion() throws Exception {
        SvgToPdfService service = new SvgToPdfService();
        
        String pathSvg = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100\" height=\"100\">\n" +
                "  <path d=\"M10,10 L90,50 L10,90 Z\" fill=\"green\" stroke=\"black\" stroke-width=\"2\"/>\n" +
                "  <ellipse cx=\"50\" cy=\"50\" rx=\"30\" ry=\"20\" fill=\"yellow\" opacity=\"0.7\"/>\n" +
                "</svg>";
        
        MockMultipartFile svgFile = new MockMultipartFile("file", "paths.svg", "image/svg+xml", pathSvg.getBytes());
        File pdfFile = new File(System.getProperty("java.io.tmpdir") + "/paths_test.pdf");
        
        service.convertSvgToPdf(svgFile, pdfFile);
        
        assertTrue(pdfFile.exists(), "PDF file should be created");
        assertTrue(pdfFile.length() > 500, "PDF file should have reasonable size");
        
        System.out.println("SVG with paths conversion successful!");
        System.out.println("Output file: " + pdfFile.getAbsolutePath());
        System.out.println("File size: " + pdfFile.length() + " bytes");
        
        // Clean up
        pdfFile.delete();
    }
}