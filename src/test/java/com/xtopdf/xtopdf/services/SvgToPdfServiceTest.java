package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.*;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SvgToPdfServiceTest {

    private SvgToPdfService svgToPdfService;

    @BeforeEach
    void setUp() {
        svgToPdfService = new SvgToPdfService(new PdfBoxBackend());
    }

    @Test
    void testConvertSvgToPdf_SimpleRectangle_Success(@TempDir Path tempDir) throws Exception {
        String svgContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100\" height=\"100\">\n" +
                "  <rect width=\"100\" height=\"100\" fill=\"blue\"/>\n" +
                "</svg>";
        byte[] svgData = svgContent.getBytes();
        MockMultipartFile svgFile = new MockMultipartFile(
                "file", 
                "test.svg", 
                "image/svg+xml", 
                svgData
        );

        File pdfFile = tempDir.resolve("testSvgOutput.pdf").toFile();

        svgToPdfService.convertSvgToPdf(svgFile, pdfFile);

        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
    }

    @Test
    void testConvertSvgToPdf_WithCircle_Success(@TempDir Path tempDir) throws Exception {
        String svgContent = "<?xml version=\"1.0\"?>\n" +
                "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"200\" height=\"200\">\n" +
                "  <circle cx=\"100\" cy=\"100\" r=\"50\" fill=\"red\"/>\n" +
                "</svg>";
        MockMultipartFile svgFile = new MockMultipartFile(
                "file", 
                "circle.svg", 
                "image/svg+xml", 
                svgContent.getBytes()
        );

        File pdfFile = tempDir.resolve("circle_output.pdf").toFile();

        svgToPdfService.convertSvgToPdf(svgFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertSvgToPdf_WithPath_Success(@TempDir Path tempDir) throws Exception {
        String svgContent = "<?xml version=\"1.0\"?>\n" +
                "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"200\" height=\"200\">\n" +
                "  <path d=\"M10 10 L 90 90\" stroke=\"black\" stroke-width=\"2\"/>\n" +
                "</svg>";
        MockMultipartFile svgFile = new MockMultipartFile(
                "file", 
                "path.svg", 
                "image/svg+xml", 
                svgContent.getBytes()
        );

        File pdfFile = tempDir.resolve("path_output.pdf").toFile();

        svgToPdfService.convertSvgToPdf(svgFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertSvgToPdf_WithText_Success(@TempDir Path tempDir) throws Exception {
        String svgContent = "<?xml version=\"1.0\"?>\n" +
                "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"300\" height=\"100\">\n" +
                "  <text x=\"10\" y=\"50\" font-size=\"20\" fill=\"black\">Hello SVG!</text>\n" +
                "</svg>";
        MockMultipartFile svgFile = new MockMultipartFile(
                "file", 
                "text.svg", 
                "image/svg+xml", 
                svgContent.getBytes()
        );

        File pdfFile = tempDir.resolve("text_output.pdf").toFile();

        svgToPdfService.convertSvgToPdf(svgFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertSvgToPdf_WithGradient_Success(@TempDir Path tempDir) throws Exception {
        String svgContent = "<?xml version=\"1.0\"?>\n" +
                "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"200\" height=\"200\">\n" +
                "  <defs>\n" +
                "    <linearGradient id=\"grad1\" x1=\"0%\" y1=\"0%\" x2=\"100%\" y2=\"0%\">\n" +
                "      <stop offset=\"0%\" style=\"stop-color:rgb(255,255,0);stop-opacity:1\" />\n" +
                "      <stop offset=\"100%\" style=\"stop-color:rgb(255,0,0);stop-opacity:1\" />\n" +
                "    </linearGradient>\n" +
                "  </defs>\n" +
                "  <rect width=\"200\" height=\"200\" fill=\"url(#grad1)\"/>\n" +
                "</svg>";
        MockMultipartFile svgFile = new MockMultipartFile(
                "file", 
                "gradient.svg", 
                "image/svg+xml", 
                svgContent.getBytes()
        );

        File pdfFile = tempDir.resolve("gradient_output.pdf").toFile();

        svgToPdfService.convertSvgToPdf(svgFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertSvgToPdf_WithMultipleShapes_Success(@TempDir Path tempDir) throws Exception {
        String svgContent = "<?xml version=\"1.0\"?>\n" +
                "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"300\" height=\"300\">\n" +
                "  <rect x=\"10\" y=\"10\" width=\"100\" height=\"100\" fill=\"blue\"/>\n" +
                "  <circle cx=\"200\" cy=\"60\" r=\"40\" fill=\"red\"/>\n" +
                "  <line x1=\"10\" y1=\"150\" x2=\"290\" y2=\"150\" stroke=\"green\" stroke-width=\"3\"/>\n" +
                "</svg>";
        MockMultipartFile svgFile = new MockMultipartFile(
                "file", 
                "multiple.svg", 
                "image/svg+xml", 
                svgContent.getBytes()
        );

        File pdfFile = tempDir.resolve("multiple_output.pdf").toFile();

        svgToPdfService.convertSvgToPdf(svgFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertSvgToPdf_WithGroup_Success(@TempDir Path tempDir) throws Exception {
        String svgContent = "<?xml version=\"1.0\"?>\n" +
                "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"200\" height=\"200\">\n" +
                "  <g transform=\"translate(50,50)\">\n" +
                "    <rect width=\"50\" height=\"50\" fill=\"yellow\"/>\n" +
                "    <circle cx=\"25\" cy=\"25\" r=\"10\" fill=\"black\"/>\n" +
                "  </g>\n" +
                "</svg>";
        MockMultipartFile svgFile = new MockMultipartFile(
                "file", 
                "group.svg", 
                "image/svg+xml", 
                svgContent.getBytes()
        );

        File pdfFile = tempDir.resolve("group_output.pdf").toFile();

        svgToPdfService.convertSvgToPdf(svgFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertSvgToPdf_WithStroke_Success(@TempDir Path tempDir) throws Exception {
        String svgContent = "<?xml version=\"1.0\"?>\n" +
                "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"200\" height=\"200\">\n" +
                "  <rect x=\"50\" y=\"50\" width=\"100\" height=\"100\" " +
                "        fill=\"none\" stroke=\"purple\" stroke-width=\"5\"/>\n" +
                "</svg>";
        MockMultipartFile svgFile = new MockMultipartFile(
                "file", 
                "stroke.svg", 
                "image/svg+xml", 
                svgContent.getBytes()
        );

        File pdfFile = tempDir.resolve("stroke_output.pdf").toFile();

        svgToPdfService.convertSvgToPdf(svgFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertSvgToPdf_NullMultipartFile_ThrowsIOException(@TempDir Path tempDir) {
        File pdfFile = tempDir.resolve("nullInput.pdf").toFile();
        assertThrows(IOException.class, 
            () -> svgToPdfService.convertSvgToPdf(null, pdfFile));
    }

    @Test
    void testConvertSvgToPdf_NullOutputFile_ThrowsIOException() {
        String svgContent = "<svg xmlns=\"http://www.w3.org/2000/svg\"></svg>";
        MockMultipartFile svgFile = new MockMultipartFile(
                "file", 
                "test.svg", 
                "image/svg+xml", 
                svgContent.getBytes()
        );
        assertThrows(IOException.class, 
            () -> svgToPdfService.convertSvgToPdf(svgFile, null));
    }

    @Test
    void testConvertSvgToPdf_EmptyFile_ThrowsIOException(@TempDir Path tempDir) {
        MockMultipartFile svgFile = new MockMultipartFile(
                "file", 
                "empty.svg", 
                "image/svg+xml", 
                new byte[0]
        );
        File pdfFile = tempDir.resolve("empty_output.pdf").toFile();

        assertThrows(IOException.class, 
            () -> svgToPdfService.convertSvgToPdf(svgFile, pdfFile));
    }
}
