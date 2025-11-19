package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PngToPdfServiceTest {

    private PngToPdfService pngToPdfService;

    @BeforeEach
    void setUp() {
        pngToPdfService = new PngToPdfService();
    }

    private byte[] createSimplePNG(int width, int height, Color color) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }

    @Test
    void testConvertPngToPdf_SimpleImage_Success(@TempDir Path tempDir) throws Exception {
        byte[] pngData = createSimplePNG(100, 100, Color.RED);
        MockMultipartFile pngFile = new MockMultipartFile(
                "file", 
                "test.png", 
                "image/png", 
                pngData
        );

        File pdfFile = tempDir.resolve("output.pdf").toFile();

        pngToPdfService.convertPngToPdf(pngFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertPngToPdf_SmallImage_Success(@TempDir Path tempDir) throws Exception {
        byte[] pngData = createSimplePNG(50, 50, Color.BLUE);
        MockMultipartFile pngFile = new MockMultipartFile(
                "file", 
                "small.png", 
                "image/png", 
                pngData
        );

        File pdfFile = tempDir.resolve("small_output.pdf").toFile();

        pngToPdfService.convertPngToPdf(pngFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertPngToPdf_LargeImage_Success(@TempDir Path tempDir) throws Exception {
        byte[] pngData = createSimplePNG(800, 600, Color.GREEN);
        MockMultipartFile pngFile = new MockMultipartFile(
                "file", 
                "large.png", 
                "image/png", 
                pngData
        );

        File pdfFile = tempDir.resolve("large_output.pdf").toFile();

        pngToPdfService.convertPngToPdf(pngFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertPngToPdf_WideImage_Success(@TempDir Path tempDir) throws Exception {
        byte[] pngData = createSimplePNG(1000, 200, Color.YELLOW);
        MockMultipartFile pngFile = new MockMultipartFile(
                "file", 
                "wide.png", 
                "image/png", 
                pngData
        );

        File pdfFile = tempDir.resolve("wide_output.pdf").toFile();

        pngToPdfService.convertPngToPdf(pngFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertPngToPdf_TallImage_Success(@TempDir Path tempDir) throws Exception {
        byte[] pngData = createSimplePNG(200, 1000, Color.CYAN);
        MockMultipartFile pngFile = new MockMultipartFile(
                "file", 
                "tall.png", 
                "image/png", 
                pngData
        );

        File pdfFile = tempDir.resolve("tall_output.pdf").toFile();

        pngToPdfService.convertPngToPdf(pngFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertPngToPdf_TransparentImage_Success(@TempDir Path tempDir) throws Exception {
        byte[] pngData = createSimplePNG(200, 200, new Color(255, 0, 0, 128));
        MockMultipartFile pngFile = new MockMultipartFile(
                "file", 
                "transparent.png", 
                "image/png", 
                pngData
        );

        File pdfFile = tempDir.resolve("transparent_output.pdf").toFile();

        pngToPdfService.convertPngToPdf(pngFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertPngToPdf_NullMultipartFile_ThrowsNullPointerException(@TempDir Path tempDir) {
        File pdfFile = tempDir.resolve("nullInput.pdf").toFile();
        assertThrows(NullPointerException.class, 
            () -> pngToPdfService.convertPngToPdf(null, pdfFile));
    }

    @Test
    void testConvertPngToPdf_NullOutputFile_ThrowsNullPointerException() throws Exception {
        byte[] pngData = createSimplePNG(100, 100, Color.RED);
        MockMultipartFile pngFile = new MockMultipartFile(
                "file", 
                "test.png", 
                "image/png", 
                pngData
        );
        assertThrows(NullPointerException.class, 
            () -> pngToPdfService.convertPngToPdf(pngFile, null));
    }

    @Test
    void testConvertPngToPdf_EmptyPngFile_ThrowsIOException(@TempDir Path tempDir) {
        MockMultipartFile pngFile = new MockMultipartFile(
                "file", 
                "empty.png", 
                "image/png", 
                new byte[0]
        );
        File pdfFile = tempDir.resolve("emptyPngOutput.pdf").toFile();
        
        assertThrows(IOException.class, 
            () -> pngToPdfService.convertPngToPdf(pngFile, pdfFile));
    }

    @Test
    void testConvertPngToPdf_InvalidPngData_ThrowsIOException(@TempDir Path tempDir) {
        byte[] invalidData = "This is not a PNG file".getBytes();
        MockMultipartFile pngFile = new MockMultipartFile(
                "file", 
                "invalid.png", 
                "image/png", 
                invalidData
        );
        File pdfFile = tempDir.resolve("invalidPngOutput.pdf").toFile();
        
        assertThrows(IOException.class, 
            () -> pngToPdfService.convertPngToPdf(pngFile, pdfFile));
    }

    @Test
    void testConvertPngToPdf_SquareImage_Success(@TempDir Path tempDir) throws Exception {
        byte[] pngData = createSimplePNG(500, 500, Color.MAGENTA);
        MockMultipartFile pngFile = new MockMultipartFile(
                "file", 
                "square.png", 
                "image/png", 
                pngData
        );

        File pdfFile = tempDir.resolve("square_output.pdf").toFile();

        pngToPdfService.convertPngToPdf(pngFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }
}