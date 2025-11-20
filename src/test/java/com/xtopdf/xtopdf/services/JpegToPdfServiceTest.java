package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class JpegToPdfServiceTest {

    private JpegToPdfService jpegToPdfService;

    @BeforeEach
    void setUp() {
        jpegToPdfService = new JpegToPdfService(new com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend());
    }

    @Test
    void convertJpegToPdf_ValidJpeg_CreatesPdfFile() throws IOException {
        // Create a proper JPEG file using ImageIO
        byte[] jpegBytes = createProperJpegBytes();
        MockMultipartFile jpegFile = new MockMultipartFile("test.jpeg", "test.jpeg", "image/jpeg", jpegBytes);
        
        Path tempDir = Files.createTempDirectory("jpeg-test");
        File pdfFile = tempDir.resolve("output.pdf").toFile();
        
        try {
            jpegToPdfService.convertJpegToPdf(jpegFile, pdfFile);
            
            assertTrue(pdfFile.exists());
            assertTrue(pdfFile.length() > 0);
        } finally {
            if (pdfFile.exists()) {
                pdfFile.delete();
            }
            Files.deleteIfExists(tempDir);
        }
    }

    @Test
    void convertJpegToPdf_EmptyFile_ThrowsIOException() {
        MockMultipartFile emptyFile = new MockMultipartFile("empty.jpeg", "empty.jpeg", "image/jpeg", new byte[0]);
        File pdfFile = new File("output.pdf");
        
        assertThrows(IOException.class, () -> jpegToPdfService.convertJpegToPdf(emptyFile, pdfFile));
    }

    @Test
    void convertJpegToPdf_InvalidJpegData_ThrowsIOException() {
        byte[] invalidData = "Not a JPEG file".getBytes();
        MockMultipartFile invalidFile = new MockMultipartFile("invalid.jpeg", "invalid.jpeg", "image/jpeg", invalidData);
        File pdfFile = new File("output.pdf");
        
        assertThrows(IOException.class, () -> jpegToPdfService.convertJpegToPdf(invalidFile, pdfFile));
    }

    private byte[] createProperJpegBytes() throws IOException {
        // Create a proper JPEG using ImageIO
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, 10, 10);
        g.dispose();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "JPEG", baos);
        return baos.toByteArray();
    }
}