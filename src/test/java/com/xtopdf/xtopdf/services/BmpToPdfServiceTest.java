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

class BmpToPdfServiceTest {

    private BmpToPdfService bmpToPdfService;

    @BeforeEach
    void setUp() {
        bmpToPdfService = new BmpToPdfService(new com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend());
    }

    private byte[] createSimpleBMP(int width, int height, Color color) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "BMP", baos);
        return baos.toByteArray();
    }

    @Test
    void testConvertBmpToPdf_SimpleImage_Success(@TempDir Path tempDir) throws Exception {
        byte[] bmpData = createSimpleBMP(100, 100, Color.RED);
        MockMultipartFile bmpFile = new MockMultipartFile(
                "file", 
                "test.bmp", 
                "image/bmp", 
                bmpData
        );

        File pdfFile = tempDir.resolve("output.pdf").toFile();

        bmpToPdfService.convertBmpToPdf(bmpFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertBmpToPdf_LargeImage_Success(@TempDir Path tempDir) throws Exception {
        byte[] bmpData = createSimpleBMP(800, 600, Color.BLUE);
        MockMultipartFile bmpFile = new MockMultipartFile(
                "file", 
                "large.bmp", 
                "image/bmp", 
                bmpData
        );

        File pdfFile = tempDir.resolve("large_output.pdf").toFile();

        bmpToPdfService.convertBmpToPdf(bmpFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertBmpToPdf_SmallImage_Success(@TempDir Path tempDir) throws Exception {
        byte[] bmpData = createSimpleBMP(50, 50, Color.GREEN);
        MockMultipartFile bmpFile = new MockMultipartFile(
                "file", 
                "small.bmp", 
                "image/bmp", 
                bmpData
        );

        File pdfFile = tempDir.resolve("small_output.pdf").toFile();

        bmpToPdfService.convertBmpToPdf(bmpFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertBmpToPdf_WideImage_Success(@TempDir Path tempDir) throws Exception {
        byte[] bmpData = createSimpleBMP(1000, 200, Color.YELLOW);
        MockMultipartFile bmpFile = new MockMultipartFile(
                "file", 
                "wide.bmp", 
                "image/bmp", 
                bmpData
        );

        File pdfFile = tempDir.resolve("wide_output.pdf").toFile();

        bmpToPdfService.convertBmpToPdf(bmpFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertBmpToPdf_TallImage_Success(@TempDir Path tempDir) throws Exception {
        byte[] bmpData = createSimpleBMP(200, 800, Color.CYAN);
        MockMultipartFile bmpFile = new MockMultipartFile(
                "file", 
                "tall.bmp", 
                "image/bmp", 
                bmpData
        );

        File pdfFile = tempDir.resolve("tall_output.pdf").toFile();

        bmpToPdfService.convertBmpToPdf(bmpFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertBmpToPdf_SquareImage_Success(@TempDir Path tempDir) throws Exception {
        byte[] bmpData = createSimpleBMP(500, 500, Color.MAGENTA);
        MockMultipartFile bmpFile = new MockMultipartFile(
                "file", 
                "square.bmp", 
                "image/bmp", 
                bmpData
        );

        File pdfFile = tempDir.resolve("square_output.pdf").toFile();

        bmpToPdfService.convertBmpToPdf(bmpFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertBmpToPdf_InvalidBmp_ThrowsIOException(@TempDir Path tempDir) throws Exception {
        // Invalid BMP will throw IOException
        byte[] bmpData = new byte[] { 0x42, 0x4D, 0x36, 0x00, 0x00, 0x00 };
        MockMultipartFile bmpFile = new MockMultipartFile(
                "file", 
                "test.bmp", 
                "image/bmp", 
                bmpData
        );

        File pdfFile = tempDir.resolve("testBmpOutput.pdf").toFile();

        assertThrows(IOException.class, 
            () -> bmpToPdfService.convertBmpToPdf(bmpFile, pdfFile));
    }

    @Test
    void testConvertBmpToPdf_EmptyFile_ThrowsException(@TempDir Path tempDir) {
        MockMultipartFile bmpFile = new MockMultipartFile(
                "file", 
                "empty.bmp", 
                "image/bmp", 
                new byte[0]
        );
        File pdfFile = tempDir.resolve("empty_output.pdf").toFile();

        assertThrows(Exception.class, 
            () -> bmpToPdfService.convertBmpToPdf(bmpFile, pdfFile));
    }

    @Test
    void testConvertBmpToPdf_NullMultipartFile_ThrowsException(@TempDir Path tempDir) {
        File pdfFile = tempDir.resolve("nullInput.pdf").toFile();
        assertThrows(Exception.class, 
            () -> bmpToPdfService.convertBmpToPdf(null, pdfFile));
    }

    @Test
    void testConvertBmpToPdf_NullOutputFile_ThrowsException() throws Exception {
        byte[] bmpData = createSimpleBMP(100, 100, Color.RED);
        MockMultipartFile bmpFile = new MockMultipartFile(
                "file", 
                "test.bmp", 
                "image/bmp", 
                bmpData
        );
        assertThrows(Exception.class, 
            () -> bmpToPdfService.convertBmpToPdf(bmpFile, null));
    }
}
