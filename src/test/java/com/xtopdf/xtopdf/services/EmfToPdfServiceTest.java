package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import com.xtopdf.xtopdf.services.conversion.image.EmfToPdfService;
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
class EmfToPdfServiceTest {

    @Mock
    private PdfBackendProvider pdfBackend;

    @Mock
    private PdfDocumentBuilder builder;

    private EmfToPdfService emfToPdfService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        lenient().when(pdfBackend.createBuilder()).thenReturn(builder);
        emfToPdfService = new EmfToPdfService(pdfBackend);
    }

    // --- Requirement 5.1: EMF renders as embedded image ---

    @Test
    void validEmfWithBoundsRendersAsImage() throws Exception {
        MockMultipartFile emfFile = createValidEmfFile("test.emf", 0, 0, 200, 150);
        File pdfFile = tempDir.resolve("output.pdf").toFile();

        emfToPdfService.convertEmfToPdf(emfFile, pdfFile);

        verify(builder).addImage(any(byte[].class));
        verify(builder, never()).addParagraph(anyString());
        verify(builder).save(pdfFile);
    }

    // --- Requirement 5.5: Fallback to text statistics when rendering fails ---

    @Test
    void invalidEmfFallsBackToTextStatistics() throws Exception {
        MockMultipartFile emfFile = new MockMultipartFile(
                "file", "invalid.emf", "application/octet-stream", "short".getBytes());
        File pdfFile = tempDir.resolve("output.pdf").toFile();

        emfToPdfService.convertEmfToPdf(emfFile, pdfFile);

        verify(builder, never()).addImage(any(byte[].class));
        verify(builder).addParagraph(contains("Enhanced Metafile Analysis"));
        verify(builder).save(pdfFile);
    }

    @Test
    void emfWithWrongRecordTypeFallsBackToStatistics() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteBuffer buffer = ByteBuffer.allocate(100);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(999); // Invalid record type
        buffer.putInt(88);
        // Pad to 88 bytes
        while (buffer.position() < 88) {
            buffer.put((byte) 0);
        }
        baos.write(buffer.array(), 0, buffer.position());

        MockMultipartFile emfFile = new MockMultipartFile(
                "file", "wrong_type.emf", "application/octet-stream", baos.toByteArray());
        File pdfFile = tempDir.resolve("output.pdf").toFile();

        emfToPdfService.convertEmfToPdf(emfFile, pdfFile);

        verify(builder, never()).addImage(any(byte[].class));
        verify(builder).addParagraph(contains("Enhanced Metafile Analysis"));
    }

    // --- Requirement 5.4: Bounds used for aspect ratio ---

    @Test
    void emfWithLargeBoundsStillRendersAsImage() throws Exception {
        // Bounds > 2000px get clamped, but rendering should still succeed
        MockMultipartFile emfFile = createValidEmfFile("large.emf", 0, 0, 5000, 5000);
        File pdfFile = tempDir.resolve("large_output.pdf").toFile();

        emfToPdfService.convertEmfToPdf(emfFile, pdfFile);

        verify(builder).addImage(any(byte[].class));
    }

    @Test
    void emfWithZeroSizeBoundsStillRendersWithMinimumDimensions() throws Exception {
        // Bounds at same position → width/height = 0, enforced to minimum 100x100
        MockMultipartFile emfFile = createValidEmfFile("tiny.emf", 50, 50, 50, 50);
        File pdfFile = tempDir.resolve("tiny_output.pdf").toFile();

        emfToPdfService.convertEmfToPdf(emfFile, pdfFile);

        verify(builder).addImage(any(byte[].class));
    }

    @Test
    void emfWithNonZeroBoundsRendersAsImage() throws Exception {
        MockMultipartFile emfFile = createValidEmfFile("test.emf", 10, 20, 310, 220);
        File pdfFile = tempDir.resolve("bounds_output.pdf").toFile();

        emfToPdfService.convertEmfToPdf(emfFile, pdfFile);

        verify(builder).addImage(any(byte[].class));
    }

    // --- Fallback statistics content verification ---

    @Test
    void fallbackStatisticsContainsFileName() throws Exception {
        MockMultipartFile emfFile = new MockMultipartFile(
                "file", "document.emf", "application/octet-stream", "short".getBytes());
        File pdfFile = tempDir.resolve("output.pdf").toFile();

        emfToPdfService.convertEmfToPdf(emfFile, pdfFile);

        verify(builder).addParagraph(contains("document.emf"));
    }

    @Test
    void fallbackStatisticsContainsFileSize() throws Exception {
        MockMultipartFile emfFile = new MockMultipartFile(
                "file", "document.emf", "application/octet-stream", "short".getBytes());
        File pdfFile = tempDir.resolve("output.pdf").toFile();

        emfToPdfService.convertEmfToPdf(emfFile, pdfFile);

        verify(builder).addParagraph(contains("File Size:"));
    }

    // --- Integration-style test with real PdfBoxBackend ---

    @Test
    void convertEmfToPdfWithRealBackendProducesValidPdf() throws Exception {
        var realBackend = new com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend();
        EmfToPdfService realService = new EmfToPdfService(realBackend);

        MockMultipartFile emfFile = createValidEmfFile("test.emf", 0, 0, 100, 100);
        File pdfFile = tempDir.resolve("real_output.pdf").toFile();

        realService.convertEmfToPdf(emfFile, pdfFile);

        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);
    }

    // --- Helper methods ---

    private MockMultipartFile createValidEmfFile(String filename, int left, int top, int right, int bottom) {
        ByteBuffer buffer = ByteBuffer.allocate(100);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // EMR_HEADER
        buffer.putInt(1);       // iType (EMR_HEADER = 1)
        buffer.putInt(88);      // nSize
        buffer.putInt(left);    // rclBounds.left
        buffer.putInt(top);     // rclBounds.top
        buffer.putInt(right);   // rclBounds.right
        buffer.putInt(bottom);  // rclBounds.bottom
        buffer.putInt(0);       // rclFrame.left
        buffer.putInt(0);       // rclFrame.top
        buffer.putInt(1000);    // rclFrame.right
        buffer.putInt(1000);    // rclFrame.bottom
        buffer.putInt(0x464D4520); // dSignature
        buffer.putInt(0x00010000); // nVersion
        buffer.putInt(100);     // nBytes
        buffer.putInt(5);       // nRecords

        // Pad to at least 88 bytes (EMF minimum header size)
        while (buffer.position() < 88) {
            buffer.put((byte) 0);
        }

        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);
        return new MockMultipartFile("file", filename, "application/octet-stream", data);
    }
}
