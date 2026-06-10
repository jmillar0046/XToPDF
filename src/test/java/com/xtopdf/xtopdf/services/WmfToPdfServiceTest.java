package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import com.xtopdf.xtopdf.services.conversion.image.WmfToPdfService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WmfToPdfServiceTest {

    @Mock
    private PdfBackendProvider pdfBackend;

    @Mock
    private PdfDocumentBuilder builder;

    private WmfToPdfService wmfToPdfService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        lenient().when(pdfBackend.createBuilder()).thenReturn(builder);
        wmfToPdfService = new WmfToPdfService(pdfBackend);
    }

    // --- Requirement 5.2: WMF renders as embedded image ---

    @Test
    void placeableWmfWithBoundsRendersAsImage() throws Exception {
        MockMultipartFile wmfFile = createPlaceableWmfFile("test.wmf", 0, 0, 200, 150);
        File pdfFile = tempDir.resolve("output.pdf").toFile();

        wmfToPdfService.convertWmfToPdf(wmfFile, pdfFile);

        verify(builder).addImage(any(byte[].class));
        verify(builder, never()).addParagraph(anyString());
        verify(builder).save(pdfFile);
    }

    // --- Requirement 5.5: Fallback to text statistics when rendering fails ---

    @Test
    void standardWmfWithoutBoundsFallsBackToStatistics() throws Exception {
        MockMultipartFile wmfFile = createStandardWmfFile("test.wmf");
        File pdfFile = tempDir.resolve("output.pdf").toFile();

        wmfToPdfService.convertWmfToPdf(wmfFile, pdfFile);

        verify(builder, never()).addImage(any(byte[].class));
        verify(builder).addParagraph(contains("Windows Metafile Analysis"));
        verify(builder).save(pdfFile);
    }

    @Test
    void invalidWmfFallsBackToTextStatistics() throws Exception {
        MockMultipartFile wmfFile = new MockMultipartFile(
                "file", "invalid.wmf", "application/octet-stream", "short".getBytes());
        File pdfFile = tempDir.resolve("output.pdf").toFile();

        wmfToPdfService.convertWmfToPdf(wmfFile, pdfFile);

        verify(builder, never()).addImage(any(byte[].class));
        verify(builder).addParagraph(contains("Windows Metafile Analysis"));
        verify(builder).save(pdfFile);
    }

    // --- Requirement 5.4: Bounds used for aspect ratio ---

    @Test
    void placeableWmfWithLargeBoundsStillRendersAsImage() throws Exception {
        MockMultipartFile wmfFile = createPlaceableWmfFile("large.wmf", 0, 0, 5000, 5000);
        File pdfFile = tempDir.resolve("large_output.pdf").toFile();

        wmfToPdfService.convertWmfToPdf(wmfFile, pdfFile);

        verify(builder).addImage(any(byte[].class));
    }

    @Test
    void placeableWmfWithZeroSizeBoundsStillRendersWithMinimumDimensions() throws Exception {
        MockMultipartFile wmfFile = createPlaceableWmfFile("tiny.wmf", 50, 50, 50, 50);
        File pdfFile = tempDir.resolve("tiny_output.pdf").toFile();

        wmfToPdfService.convertWmfToPdf(wmfFile, pdfFile);

        verify(builder).addImage(any(byte[].class));
    }

    @Test
    void placeableWmfWithNonZeroBoundsRendersAsImage() throws Exception {
        MockMultipartFile wmfFile = createPlaceableWmfFile("test.wmf", 10, 20, 310, 220);
        File pdfFile = tempDir.resolve("bounds_output.pdf").toFile();

        wmfToPdfService.convertWmfToPdf(wmfFile, pdfFile);

        verify(builder).addImage(any(byte[].class));
    }

    // --- Fallback statistics content verification ---

    @Test
    void fallbackStatisticsContainsFileName() throws Exception {
        MockMultipartFile wmfFile = new MockMultipartFile(
                "file", "document.wmf", "application/octet-stream", "short".getBytes());
        File pdfFile = tempDir.resolve("output.pdf").toFile();

        wmfToPdfService.convertWmfToPdf(wmfFile, pdfFile);

        verify(builder).addParagraph(contains("document.wmf"));
    }

    @Test
    void fallbackStatisticsContainsFileSize() throws Exception {
        MockMultipartFile wmfFile = new MockMultipartFile(
                "file", "document.wmf", "application/octet-stream", "short".getBytes());
        File pdfFile = tempDir.resolve("output.pdf").toFile();

        wmfToPdfService.convertWmfToPdf(wmfFile, pdfFile);

        verify(builder).addParagraph(contains("File Size:"));
    }

    @Test
    void fallbackStatisticsIndicatesPlaceableType() throws Exception {
        // Standard WMF triggers fallback path - test that a standard WMF shows "Standard" type
        MockMultipartFile wmfFile = createStandardWmfFile("standard.wmf");
        File pdfFile = tempDir.resolve("standard_output.pdf").toFile();

        wmfToPdfService.convertWmfToPdf(wmfFile, pdfFile);

        verify(builder).addParagraph(contains("Standard"));
    }

    // --- Integration-style test with real PdfBoxBackend ---

    @Test
    void convertWmfToPdfWithRealBackendProducesValidPdf() throws Exception {
        var realBackend = new com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend();
        WmfToPdfService realService = new WmfToPdfService(realBackend);

        MockMultipartFile wmfFile = createPlaceableWmfFile("test.wmf", 0, 0, 100, 100);
        File pdfFile = tempDir.resolve("real_output.pdf").toFile();

        realService.convertWmfToPdf(wmfFile, pdfFile);

        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);
    }

    // --- Helper methods ---

    private MockMultipartFile createPlaceableWmfFile(String filename, int left, int top, int right, int bottom) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteBuffer buffer = ByteBuffer.allocate(50);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // Placeable header
        buffer.putInt(0x9AC6CDD7); // Magic number
        buffer.putShort((short) 0); // Handle
        buffer.putShort((short) left);   // Left
        buffer.putShort((short) top);    // Top
        buffer.putShort((short) right);  // Right
        buffer.putShort((short) bottom); // Bottom
        buffer.putShort((short) 96);    // Inch
        buffer.putInt(0);               // Reserved
        buffer.putShort((short) 0);     // Checksum

        // Standard WMF header
        buffer.putShort((short) 1);      // Type
        buffer.putShort((short) 9);      // HeaderSize
        buffer.putShort((short) 0x0300); // Version
        buffer.putInt(100);              // Size
        buffer.putShort((short) 0);      // NumObjects
        buffer.putInt(10);               // MaxRecord

        baos.write(buffer.array(), 0, buffer.position());
        return new MockMultipartFile("file", filename, "application/octet-stream", baos.toByteArray());
    }

    private MockMultipartFile createStandardWmfFile(String filename) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteBuffer buffer = ByteBuffer.allocate(30);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // Standard WMF header (no placeable header)
        buffer.putShort((short) 1);      // Type
        buffer.putShort((short) 9);      // HeaderSize
        buffer.putShort((short) 0x0300); // Version
        buffer.putInt(100);              // Size
        buffer.putShort((short) 0);      // NumObjects
        buffer.putInt(10);               // MaxRecord

        baos.write(buffer.array(), 0, buffer.position());
        return new MockMultipartFile("file", filename, "application/octet-stream", baos.toByteArray());
    }
}
