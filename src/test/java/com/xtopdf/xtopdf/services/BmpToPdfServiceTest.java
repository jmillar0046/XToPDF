package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class BmpToPdfServiceTest {
    private BmpToPdfService bmpToPdfService;

    @BeforeEach
    void setUp() {
        bmpToPdfService = new BmpToPdfService();
    }

    @Test
    void testConvertBmpToPdf() throws Exception {
        var bmpFile = new MockMultipartFile("file", "test.bmp", "image/bmp", createMockBmpFileContent());
        var pdfFile = new File(System.getProperty("java.io.tmpdir") + "/testBmpOutput.pdf");

        bmpToPdfService.convertBmpToPdf(bmpFile, pdfFile);

        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
        
        // Clean up
        pdfFile.delete();
    }

    @Test
    void testConvertBmpToPdf_ValidImage() throws Exception {
        var bmpFile = new MockMultipartFile("file", "valid.bmp", "image/bmp", createMockBmpFileContent());
        var pdfFile = new File(System.getProperty("java.io.tmpdir") + "/validBmpOutput.pdf");

        bmpToPdfService.convertBmpToPdf(bmpFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
        
        // Clean up
        pdfFile.delete();
    }

    @Test
    void testConvertBmpToPdf_InvalidBmpData_ThrowsIOException() {
        var invalidBmpFile = new MockMultipartFile("file", "invalid.bmp", "image/bmp", "invalid bmp data".getBytes());
        var pdfFile = new File(System.getProperty("java.io.tmpdir") + "/invalidBmpOutput.pdf");

        IOException exception = assertThrows(IOException.class, () -> {
            bmpToPdfService.convertBmpToPdf(invalidBmpFile, pdfFile);
        });

        assertTrue(exception.getMessage().contains("Unable to read BMP image"));
    }

    @Test
    void testConvertBmpToPdf_EmptyFile_ThrowsIOException() {
        var emptyBmpFile = new MockMultipartFile("file", "empty.bmp", "image/bmp", new byte[0]);
        var pdfFile = new File(System.getProperty("java.io.tmpdir") + "/emptyBmpOutput.pdf");

        IOException exception = assertThrows(IOException.class, () -> {
            bmpToPdfService.convertBmpToPdf(emptyBmpFile, pdfFile);
        });

        assertTrue(exception.getMessage().contains("Unable to read BMP image"));
    }

    @Test
    void testConvertBmpToPdf_LargeImage() throws Exception {
        var largeBmpFile = new MockMultipartFile("file", "large.bmp", "image/bmp", createLargeMockBmpFileContent());
        var pdfFile = new File(System.getProperty("java.io.tmpdir") + "/largeBmpOutput.pdf");

        bmpToPdfService.convertBmpToPdf(largeBmpFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
        
        // Clean up
        pdfFile.delete();
    }

    @Test
    void testConvertBmpToPdf_SmallImage() throws Exception {
        var smallBmpFile = new MockMultipartFile("file", "small.bmp", "image/bmp", createSmallMockBmpFileContent());
        var pdfFile = new File(System.getProperty("java.io.tmpdir") + "/smallBmpOutput.pdf");

        bmpToPdfService.convertBmpToPdf(smallBmpFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
        
        // Clean up
        pdfFile.delete();
    }

    private byte[] createMockBmpFileContent() throws IOException {
        // Create a simple 100x100 BMP image
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.BLUE);
        g2d.fillRect(0, 0, 100, 100);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(25, 25, 50, 50);
        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "BMP", baos);
        return baos.toByteArray();
    }

    private byte[] createLargeMockBmpFileContent() throws IOException {
        // Create a larger 500x500 BMP image
        BufferedImage image = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.RED);
        g2d.fillRect(0, 0, 500, 500);
        g2d.setColor(Color.YELLOW);
        g2d.fillOval(100, 100, 300, 300);
        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "BMP", baos);
        return baos.toByteArray();
    }

    private byte[] createSmallMockBmpFileContent() throws IOException {
        // Create a small 10x10 BMP image
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.GREEN);
        g2d.fillRect(0, 0, 10, 10);
        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "BMP", baos);
        return baos.toByteArray();
    }
}