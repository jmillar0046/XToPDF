package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests to achieve 100% coverage for all services
 */
class Complete100PercentCoverageTests {

    @TempDir
    Path tempDir;

    // CSV Tests
    @Test
    void testCsvToPdfService_RealFile() throws Exception {
        CsvToPdfService service = new CsvToPdfService();
        ClassPathResource resource = new ClassPathResource("test-files/test.csv");
        try (InputStream is = resource.getInputStream()) {
            byte[] fileBytes = is.readAllBytes();
            MockMultipartFile file = new MockMultipartFile("test.csv", "test.csv",
                "text/csv", fileBytes);
            File output = tempDir.resolve("output.pdf").toFile();
            service.convertCsvToPdf(file, output);
            assertTrue(output.exists() && output.length() > 0);
        }
    }

    @Test
    void testCsvToPdfService_EmptyFile() {
        CsvToPdfService service = new CsvToPdfService();
        MockMultipartFile file = new MockMultipartFile("test.csv", "test.csv",
            "text/csv", new byte[0]);
        File output = tempDir.resolve("output.pdf").toFile();
        assertThrows(Exception.class, () -> service.convertCsvToPdf(file, output));
    }

    @Test
    void testCsvToPdfService_WithQuotes() throws Exception {
        CsvToPdfService service = new CsvToPdfService();
        String csvWithQuotes = "\"Name\",\"Value\"\n\"Item 1\",\"100\"\n\"Item, with comma\",\"200\"";
        MockMultipartFile file = new MockMultipartFile("test.csv", "test.csv",
            "text/csv", csvWithQuotes.getBytes());
        File output = tempDir.resolve("output.pdf").toFile();
        service.convertCsvToPdf(file, output);
        assertTrue(output.exists() && output.length() > 0);
    }

    // Text Tests
    @Test
    void testTxtToPdfService_RealFile() throws Exception {
        TxtToPdfService service = new TxtToPdfService();
        ClassPathResource resource = new ClassPathResource("test-files/test.txt");
        try (InputStream is = resource.getInputStream()) {
            byte[] fileBytes = is.readAllBytes();
            MockMultipartFile file = new MockMultipartFile("test.txt", "test.txt",
                "text/plain", fileBytes);
            File output = tempDir.resolve("output.pdf").toFile();
            service.convertTxtToPdf(file, output);
            assertTrue(output.exists() && output.length() > 0);
        }
    }

    // HTML Tests
    @Test
    void testHtmlToPdfService_RealFile() throws Exception {
        HtmlToPdfService service = new HtmlToPdfService();
        ClassPathResource resource = new ClassPathResource("test-files/test.html");
        try (InputStream is = resource.getInputStream()) {
            byte[] fileBytes = is.readAllBytes();
            MockMultipartFile file = new MockMultipartFile("test.html", "test.html",
                "text/html", fileBytes);
            File output = tempDir.resolve("output.pdf").toFile();
            service.convertHtmlToPdf(file, output);
            assertTrue(output.exists() && output.length() > 0);
        }
    }

    // JSON Tests
    @Test
    void testJsonToPdfService_RealFile() throws Exception {
        JsonToPdfService service = new JsonToPdfService();
        ClassPathResource resource = new ClassPathResource("test-files/test.json");
        try (InputStream is = resource.getInputStream()) {
            byte[] fileBytes = is.readAllBytes();
            MockMultipartFile file = new MockMultipartFile("test.json", "test.json",
                "application/json", fileBytes);
            File output = tempDir.resolve("output.pdf").toFile();
            service.convertJsonToPdf(file, output);
            assertTrue(output.exists() && output.length() > 0);
        }
    }

    // XML Tests
    @Test
    void testXmlToPdfService_RealFile() throws Exception {
        XmlToPdfService service = new XmlToPdfService();
        ClassPathResource resource = new ClassPathResource("test-files/test.xml");
        try (InputStream is = resource.getInputStream()) {
            byte[] fileBytes = is.readAllBytes();
            MockMultipartFile file = new MockMultipartFile("test.xml", "test.xml",
                "application/xml", fileBytes);
            File output = tempDir.resolve("output.pdf").toFile();
            service.convertXmlToPdf(file, output);
            assertTrue(output.exists() && output.length() > 0);
        }
    }

    // Markdown Tests
    @Test
    void testMarkdownToPdfService_RealFile() throws Exception {
        MarkdownToPdfService service = new MarkdownToPdfService();
        ClassPathResource resource = new ClassPathResource("test-files/test.md");
        try (InputStream is = resource.getInputStream()) {
            byte[] fileBytes = is.readAllBytes();
            MockMultipartFile file = new MockMultipartFile("test.md", "test.md",
                "text/markdown", fileBytes);
            File output = tempDir.resolve("output.pdf").toFile();
            service.convertMarkdownToPdf(file, output);
            assertTrue(output.exists() && output.length() > 0);
        }
    }

    // SVG Tests
    @Test
    void testSvgToPdfService_RealFile() throws Exception {
        SvgToPdfService service = new SvgToPdfService();
        ClassPathResource resource = new ClassPathResource("test-files/test.svg");
        try (InputStream is = resource.getInputStream()) {
            byte[] fileBytes = is.readAllBytes();
            MockMultipartFile file = new MockMultipartFile("test.svg", "test.svg",
                "image/svg+xml", fileBytes);
            File output = tempDir.resolve("output.pdf").toFile();
            service.convertSvgToPdf(file, output);
            assertTrue(output.exists() && output.length() > 0);
        }
    }

    // Image service tests - PNG
    @Test
    void testPngToPdfService_ValidFile() throws Exception {
        PngToPdfService service = new PngToPdfService();
        // Create a minimal PNG
        byte[] pngData = new byte[]{
            (byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
            0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
            0x08, 0x02, 0x00, 0x00, 0x00, (byte)0x90, (byte)0x77, 0x53, (byte)0xDE,
            0x00, 0x00, 0x00, 0x0C, 0x49, 0x44, 0x41, 0x54,
            0x08, (byte)0xD7, 0x63, (byte)0xF8, (byte)0xCF, (byte)0xC0, 0x00, 0x00,
            0x03, 0x01, 0x01, 0x00, 0x18, (byte)0xDD, (byte)0x8D, (byte)0xB4,
            0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44,
            (byte)0xAE, 0x42, 0x60, (byte)0x82
        };
        MockMultipartFile file = new MockMultipartFile("test.png", "test.png",
            "image/png", pngData);
        File output = tempDir.resolve("output.pdf").toFile();
        service.convertPngToPdf(file, output);
        assertTrue(output.exists() && output.length() > 0);
    }

    @Test
    void testPngToPdfService_InvalidFile() {
        PngToPdfService service = new PngToPdfService();
        MockMultipartFile file = new MockMultipartFile("test.png", "test.png",
            "image/png", "not a png".getBytes());
        File output = tempDir.resolve("output.pdf").toFile();
        assertThrows(Exception.class, () -> service.convertPngToPdf(file, output));
    }

    @Test
    void testPngToPdfService_EmptyFile() {
        PngToPdfService service = new PngToPdfService();
        MockMultipartFile file = new MockMultipartFile("test.png", "test.png",
            "image/png", new byte[0]);
        File output = tempDir.resolve("output.pdf").toFile();
        assertThrows(Exception.class, () -> service.convertPngToPdf(file, output));
    }

    // GIF Tests
    @Test
    void testGifToPdfService_ValidFile() throws Exception {
        GifToPdfService service = new GifToPdfService();
        // Minimal GIF87a header
        byte[] gifData = new byte[]{
            0x47, 0x49, 0x46, 0x38, 0x37, 0x61, // GIF87a
            0x01, 0x00, 0x01, 0x00, // 1x1 logical screen
            (byte)0x80, 0x00, 0x00, // Global color table flag
            (byte)0xFF, (byte)0xFF, (byte)0xFF, // White
            0x00, 0x00, 0x00, // Black
            0x2C, 0x00, 0x00, 0x00, 0x00, // Image descriptor
            0x01, 0x00, 0x01, 0x00, 0x00, // 1x1 image
            0x02, 0x02, 0x44, 0x01, 0x00, // Image data
            0x3B // Trailer
        };
        MockMultipartFile file = new MockMultipartFile("test.gif", "test.gif",
            "image/gif", gifData);
        File output = tempDir.resolve("output.pdf").toFile();
        service.convertGifToPdf(file, output);
        assertTrue(output.exists() && output.length() > 0);
    }

    @Test
    void testGifToPdfService_InvalidFile() {
        GifToPdfService service = new GifToPdfService();
        MockMultipartFile file = new MockMultipartFile("test.gif", "test.gif",
            "image/gif", "not a gif".getBytes());
        File output = tempDir.resolve("output.pdf").toFile();
        assertThrows(Exception.class, () -> service.convertGifToPdf(file, output));
    }

    // TIFF Tests
    @Test
    void testTiffToPdfService_InvalidFile() {
        TiffToPdfService service = new TiffToPdfService();
        MockMultipartFile file = new MockMultipartFile("test.tiff", "test.tiff",
            "image/tiff", "not a tiff".getBytes());
        File output = tempDir.resolve("output.pdf").toFile();
        assertThrows(Exception.class, () -> service.convertTiffToPdf(file, output));
    }

    // RTF Tests
    @Test
    void testRtfToPdfService_ValidFile() throws Exception {
        RtfToPdfService service = new RtfToPdfService();
        String rtfContent = "{\\rtf1\\ansi\\deff0 {\\fonttbl {\\f0 Arial;}} \\f0\\fs24 Test RTF Document}";
        MockMultipartFile file = new MockMultipartFile("test.rtf", "test.rtf",
            "application/rtf", rtfContent.getBytes());
        File output = tempDir.resolve("output.pdf").toFile();
        service.convertRtfToPdf(file, output);
        assertTrue(output.exists() && output.length() > 0);
    }
}
