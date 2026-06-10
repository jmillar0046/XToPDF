package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import com.xtopdf.xtopdf.pdf.impl.PdfBoxDocumentBuilder;
import com.xtopdf.xtopdf.services.conversion.image.JpegToPdfService;
import com.xtopdf.xtopdf.services.conversion.image.PngToPdfService;
import net.jqwik.api.*;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * Property-based tests for image conversion services (PngToPdfService, JpegToPdfService).
 *
 * Verifies that various image dimensions produce valid PDFs without crashing.
 *
 * **Validates: Requirements 32.2**
 */
class ImageConversionPropertyTest {

    // Simple backend provider that creates builders with null fonts (uses Helvetica fallback)
    private final PdfBackendProvider pdfBackend = new PdfBackendProvider() {
        @Override
        public PdfDocumentBuilder createBuilder() throws IOException {
            return new PdfBoxDocumentBuilder(null, null, null);
        }

        @Override
        public String getBackendName() {
            return "pdfbox-test";
        }
    };

    @Property(tries = 25)
    @Tag("Feature: advanced-improvements, Property: PNG conversion produces valid PDF for various dimensions")
    void pngConversionProducesValidPdf(
            @ForAll("imageWidths") int width,
            @ForAll("imageHeights") int height) throws IOException {

        PngToPdfService service = new PngToPdfService(pdfBackend);
        byte[] pngBytes = createPngImage(width, height);
        MockMultipartFile pngFile = new MockMultipartFile(
                "file", "test.png", "image/png", pngBytes);

        File outputPdf = Files.createTempFile("test_png_", ".pdf").toFile();
        try {
            service.convertPngToPdf(pngFile, outputPdf);

            assertThat(outputPdf).exists();
            assertThat(outputPdf.length()).isGreaterThan(0);

            // Verify valid PDF by checking magic bytes
            byte[] pdfContent = Files.readAllBytes(outputPdf.toPath());
            assertThat(pdfContent).hasSizeGreaterThan(4);
            assertThat(new String(pdfContent, 0, 4)).isEqualTo("%PDF");
        } finally {
            outputPdf.delete();
        }
    }

    @Property(tries = 25)
    @Tag("Feature: advanced-improvements, Property: JPEG conversion produces valid PDF for various dimensions")
    void jpegConversionProducesValidPdf(
            @ForAll("imageWidths") int width,
            @ForAll("imageHeights") int height) throws IOException {

        JpegToPdfService service = new JpegToPdfService(pdfBackend);
        byte[] jpegBytes = createJpegImage(width, height);
        MockMultipartFile jpegFile = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", jpegBytes);

        File outputPdf = Files.createTempFile("test_jpeg_", ".pdf").toFile();
        try {
            service.convertJpegToPdf(jpegFile, outputPdf);

            assertThat(outputPdf).exists();
            assertThat(outputPdf.length()).isGreaterThan(0);

            byte[] pdfContent = Files.readAllBytes(outputPdf.toPath());
            assertThat(pdfContent).hasSizeGreaterThan(4);
            assertThat(new String(pdfContent, 0, 4)).isEqualTo("%PDF");
        } finally {
            outputPdf.delete();
        }
    }

    @Property(tries = 25)
    @Tag("Feature: advanced-improvements, Property: PNG conversion does not crash on various image sizes")
    void pngConversionDoesNotCrash(
            @ForAll("imageWidths") int width,
            @ForAll("imageHeights") int height) {

        PngToPdfService service = new PngToPdfService(pdfBackend);

        assertThatNoException().isThrownBy(() -> {
            byte[] pngBytes = createPngImage(width, height);
            MockMultipartFile pngFile = new MockMultipartFile(
                    "file", "test.png", "image/png", pngBytes);
            File outputPdf = Files.createTempFile("test_png_nocrash_", ".pdf").toFile();
            try {
                service.convertPngToPdf(pngFile, outputPdf);
            } finally {
                outputPdf.delete();
            }
        });
    }

    // ---------------------------------------------------------------
    // Generators
    // ---------------------------------------------------------------

    @Provide
    Arbitrary<Integer> imageWidths() {
        return Arbitraries.integers().between(1, 2000);
    }

    @Provide
    Arbitrary<Integer> imageHeights() {
        return Arbitraries.integers().between(1, 2000);
    }

    // ---------------------------------------------------------------
    // Helper Methods
    // ---------------------------------------------------------------

    private byte[] createPngImage(int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // Fill with a simple gradient pattern
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = ((x * 255 / Math.max(width, 1)) << 16)
                        | ((y * 255 / Math.max(height, 1)) << 8)
                        | 128;
                image.setRGB(x, y, rgb);
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }

    private byte[] createJpegImage(int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = ((x * 128 / Math.max(width, 1)) << 16)
                        | ((y * 128 / Math.max(height, 1)) << 8)
                        | 64;
                image.setRGB(x, y, rgb);
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "JPEG", baos);
        return baos.toByteArray();
    }
}
