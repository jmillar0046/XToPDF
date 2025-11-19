package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.*;

class TiffToPdfServiceTest {

    private final TiffToPdfService tiffToPdfService = new TiffToPdfService(new com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend());

    @TempDir
    Path tempDir;

    @Test
    void testConvertTiffToPdf() throws IOException {
        // Create a simple test TIFF image
        BufferedImage testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = testImage.createGraphics();
        g2d.setColor(Color.BLUE);
        g2d.fillRect(0, 0, 100, 100);
        g2d.dispose();

        // Convert to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(testImage, "tiff", baos);
        byte[] tiffBytes = baos.toByteArray();

        // Create MockMultipartFile
        MockMultipartFile tiffFile = new MockMultipartFile(
            "testFile", "test.tiff", "image/tiff", tiffBytes);

        // Create output file
        File outputFile = tempDir.resolve("output.pdf").toFile();

        // Test conversion
        tiffToPdfService.convertTiffToPdf(tiffFile, outputFile);

        // Verify output file exists and has content
        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    @Test
    void testConvertTiffToPdf_InvalidTiffFile_ThrowsIOException() {
        // Create invalid TIFF file content
        byte[] invalidTiffBytes = "Not a TIFF file".getBytes();
        MockMultipartFile invalidTiffFile = new MockMultipartFile(
            "testFile", "test.tiff", "image/tiff", invalidTiffBytes);

        File outputFile = tempDir.resolve("output.pdf").toFile();

        // Test that conversion throws IOException for invalid TIFF
        assertThrows(IOException.class, () -> 
            tiffToPdfService.convertTiffToPdf(invalidTiffFile, outputFile));
    }

    @Test
    void testConvertTiffToPdf_EmptyFile_ThrowsIOException() {
        // Create empty TIFF file
        MockMultipartFile emptyTiffFile = new MockMultipartFile(
            "testFile", "test.tiff", "image/tiff", new byte[0]);

        File outputFile = tempDir.resolve("output.pdf").toFile();

        // Test that conversion throws IOException for empty file
        assertThrows(IOException.class, () -> 
            tiffToPdfService.convertTiffToPdf(emptyTiffFile, outputFile));
    }
}