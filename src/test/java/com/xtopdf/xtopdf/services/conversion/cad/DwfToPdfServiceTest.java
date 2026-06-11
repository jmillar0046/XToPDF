package com.xtopdf.xtopdf.services.conversion.cad;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend;
import com.xtopdf.xtopdf.services.conversion.cad.DwfToPdfService.DwfEntry;
import com.xtopdf.xtopdf.services.conversion.cad.DwfToPdfService.DwfPackageInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DwfToPdfServiceTest {

    @Mock
    private PdfBackendProvider mockBackendProvider;

    @Mock
    private PdfDocumentBuilder mockBuilder;

    private DwfToPdfService service;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        when(mockBackendProvider.createBuilder()).thenReturn(mockBuilder);
        service = new DwfToPdfService(mockBackendProvider);
    }

    // --- ZIP with embedded PNG → addImage called ---

    @Test
    void convertDwfToPdf_zipWithEmbeddedPng_callsAddImage() throws IOException {
        byte[] pngData = createMinimalPng();
        byte[] zipBytes = createZipWithEntries(new ZipTestEntry("drawing.png", pngData));

        MockMultipartFile file = new MockMultipartFile(
            "file", "test.dwf", "application/octet-stream", zipBytes);
        File pdfFile = tempDir.resolve("output.pdf").toFile();

        service.convertDwfToPdf(file, pdfFile);

        verify(mockBuilder).newPage(595f, 842f);
        verify(mockBuilder).addImage(pngData);
        verify(mockBuilder).save(pdfFile);
    }

    // --- ZIP with multiple images → multiple pages ---

    @Test
    void convertDwfToPdf_zipWithMultipleImages_createsMultiplePages() throws IOException {
        byte[] png1 = createMinimalPng();
        byte[] png2 = createMinimalPng();
        byte[] jpgData = createMinimalJpeg();
        byte[] zipBytes = createZipWithEntries(
            new ZipTestEntry("sheet1.png", png1),
            new ZipTestEntry("sheet2.png", png2),
            new ZipTestEntry("photo.jpg", jpgData)
        );

        MockMultipartFile file = new MockMultipartFile(
            "file", "multi.dwf", "application/octet-stream", zipBytes);
        File pdfFile = tempDir.resolve("output.pdf").toFile();

        service.convertDwfToPdf(file, pdfFile);

        verify(mockBuilder, times(3)).newPage(595f, 842f);
        verify(mockBuilder, times(3)).addImage(any(byte[].class));
    }

    // --- ZIP with only XML/W2D → statistics fallback ---

    @Test
    void convertDwfToPdf_zipWithOnlyW2dAndXml_rendersStatistics() throws IOException {
        byte[] w2dData = new byte[]{0x01, 0x02, 0x03, 0x04};
        byte[] xmlData = "<Manifest><Title>Test Drawing</Title></Manifest>".getBytes();
        byte[] zipBytes = createZipWithEntries(
            new ZipTestEntry("drawing.w2d", w2dData),
            new ZipTestEntry("manifest.xml", xmlData)
        );

        MockMultipartFile file = new MockMultipartFile(
            "file", "drawing.dwf", "application/octet-stream", zipBytes);
        File pdfFile = tempDir.resolve("output.pdf").toFile();

        service.convertDwfToPdf(file, pdfFile);

        // Should not call addImage (no embedded images)
        verify(mockBuilder, never()).addImage(any(byte[].class));
        // Should render statistics page
        verify(mockBuilder).newPage(595f, 842f);
        verify(mockBuilder, atLeastOnce()).addFormattedText(contains("DWF Package Analysis"), eq(true), eq(false), eq(18f));
        verify(mockBuilder).save(pdfFile);
    }

    // --- Empty ZIP → statistics with "no content" note ---

    @Test
    void convertDwfToPdf_emptyZip_rendersEmptyPackageNote() throws IOException {
        byte[] zipBytes = createEmptyZip();

        MockMultipartFile file = new MockMultipartFile(
            "file", "empty.dwf", "application/octet-stream", zipBytes);
        File pdfFile = tempDir.resolve("output.pdf").toFile();

        service.convertDwfToPdf(file, pdfFile);

        verify(mockBuilder, never()).addImage(any(byte[].class));
        verify(mockBuilder).newPage(595f, 842f);
        verify(mockBuilder).addFormattedText(contains("Package is empty"), eq(false), eq(true), eq(11f));
        verify(mockBuilder).save(pdfFile);
    }

    // --- Invalid ZIP → IOException ---

    @Test
    void convertDwfToPdf_invalidZip_producesStatisticsFallback() throws IOException {
        // ZipInputStream silently handles most invalid data by returning no entries.
        // This means invalid ZIPs produce an empty package info → statistics fallback.
        byte[] garbage = "This is not a ZIP file at all".getBytes();

        MockMultipartFile file = new MockMultipartFile(
            "file", "bad.dwf", "application/octet-stream", garbage);
        File pdfFile = tempDir.resolve("output.pdf").toFile();

        service.convertDwfToPdf(file, pdfFile);

        // Falls through to statistics with empty entries
        verify(mockBuilder).newPage(595f, 842f);
        verify(mockBuilder).addFormattedText(contains("Package is empty"), eq(false), eq(true), eq(11f));
    }

    // --- Entry type classification ---

    @Test
    void classifyEntry_w2dFile() {
        assertThat(service.classifyEntry("drawing.w2d")).isEqualTo("Drawing (W2D)");
    }

    @Test
    void classifyEntry_pngFile() {
        assertThat(service.classifyEntry("image.png")).isEqualTo("Image");
    }

    @Test
    void classifyEntry_jpgFile() {
        assertThat(service.classifyEntry("photo.jpg")).isEqualTo("Image");
    }

    @Test
    void classifyEntry_jpegFile() {
        assertThat(service.classifyEntry("photo.jpeg")).isEqualTo("Image");
    }

    @Test
    void classifyEntry_bmpFile() {
        assertThat(service.classifyEntry("bitmap.bmp")).isEqualTo("Image");
    }

    @Test
    void classifyEntry_tiffFile() {
        assertThat(service.classifyEntry("scan.tiff")).isEqualTo("Image");
    }

    @Test
    void classifyEntry_tifFile() {
        assertThat(service.classifyEntry("scan.tif")).isEqualTo("Image");
    }

    @Test
    void classifyEntry_xmlFile() {
        assertThat(service.classifyEntry("manifest.xml")).isEqualTo("XML Metadata");
    }

    @Test
    void classifyEntry_ttfFont() {
        assertThat(service.classifyEntry("arial.ttf")).isEqualTo("Font");
    }

    @Test
    void classifyEntry_otfFont() {
        assertThat(service.classifyEntry("roboto.otf")).isEqualTo("Font");
    }

    @Test
    void classifyEntry_unknownExtension() {
        assertThat(service.classifyEntry("readme.txt")).isEqualTo("Other");
    }

    // --- Size cap enforcement ---

    @Test
    void parseDwfPackage_includesSmallImages() throws IOException {
        byte[] smallPng = createMinimalPng();
        byte[] zipBytes = createZipWithEntries(new ZipTestEntry("small.png", smallPng));

        MockMultipartFile file = new MockMultipartFile(
            "file", "test.dwf", "application/octet-stream", zipBytes);

        DwfPackageInfo info = service.parseDwfPackage(file);

        assertThat(info.embeddedImages).hasSize(1);
        assertThat(info.hasDrawingContent).isTrue();
    }

    @Test
    void parseDwfPackage_catalogsAllEntries() throws IOException {
        byte[] zipBytes = createZipWithEntries(
            new ZipTestEntry("drawing.w2d", new byte[]{1, 2, 3}),
            new ZipTestEntry("preview.png", createMinimalPng()),
            new ZipTestEntry("meta.xml", "<root/>".getBytes()),
            new ZipTestEntry("font.ttf", new byte[]{0, 1}),
            new ZipTestEntry("other.dat", new byte[]{9, 8, 7})
        );

        MockMultipartFile file = new MockMultipartFile(
            "file", "test.dwf", "application/octet-stream", zipBytes);

        DwfPackageInfo info = service.parseDwfPackage(file);

        assertThat(info.entries).hasSize(5);
        assertThat(info.entries.stream().map(DwfEntry::type))
            .containsExactly("Drawing (W2D)", "Image", "XML Metadata", "Font", "Other");
    }

    // --- XML metadata extraction ---

    @Test
    void parseDwfPackage_extractsXmlMetadata() throws IOException {
        String xml = "<Document><Title>Floor Plan</Title><Author>Architect</Author></Document>";
        byte[] zipBytes = createZipWithEntries(new ZipTestEntry("metadata.xml", xml.getBytes()));

        MockMultipartFile file = new MockMultipartFile(
            "file", "test.dwf", "application/octet-stream", zipBytes);

        DwfPackageInfo info = service.parseDwfPackage(file);

        assertThat(info.metadata).containsEntry("Title", "Floor Plan");
        assertThat(info.metadata).containsEntry("Author", "Architect");
    }

    @Test
    void parseDwfPackage_emptyZipReturnsEmptyInfo() throws IOException {
        byte[] zipBytes = createEmptyZip();
        MockMultipartFile file = new MockMultipartFile(
            "file", "empty.dwf", "application/octet-stream", zipBytes);

        DwfPackageInfo info = service.parseDwfPackage(file);

        assertThat(info.entries).isEmpty();
        assertThat(info.embeddedImages).isEmpty();
        assertThat(info.metadata).isEmpty();
        assertThat(info.hasDrawingContent).isFalse();
    }

    // --- Integration test with real PdfBoxBackend ---

    @Test
    void convertDwfToPdf_integrationWithPdfBoxBackend_statisticsFallback() throws Exception {
        DwfToPdfService realService = new DwfToPdfService(new PdfBoxBackend());

        byte[] w2dData = new byte[]{0x01, 0x02, 0x03};
        byte[] xmlData = "<Config><Title>Test</Title></Config>".getBytes();
        byte[] zipBytes = createZipWithEntries(
            new ZipTestEntry("section.w2d", w2dData),
            new ZipTestEntry("config.xml", xmlData)
        );

        MockMultipartFile file = new MockMultipartFile(
            "file", "test.dwf", "application/octet-stream", zipBytes);
        File pdfFile = tempDir.resolve("integration.pdf").toFile();

        realService.convertDwfToPdf(file, pdfFile);

        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).as("PDF should have content").isGreaterThan(0);
    }

    @Test
    void convertDwfToPdf_integrationWithPdfBoxBackend_embeddedImage() throws Exception {
        DwfToPdfService realService = new DwfToPdfService(new PdfBoxBackend());

        byte[] pngData = createMinimalPng();
        byte[] zipBytes = createZipWithEntries(new ZipTestEntry("preview.png", pngData));

        MockMultipartFile file = new MockMultipartFile(
            "file", "test.dwf", "application/octet-stream", zipBytes);
        File pdfFile = tempDir.resolve("image-integration.pdf").toFile();

        realService.convertDwfToPdf(file, pdfFile);

        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).as("PDF with image should have content").isGreaterThan(0);
    }

    // --- Helper methods ---

    private record ZipTestEntry(String name, byte[] data) {}

    private byte[] createZipWithEntries(ZipTestEntry... entries) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (ZipTestEntry entry : entries) {
                zos.putNextEntry(new ZipEntry(entry.name()));
                zos.write(entry.data());
                zos.closeEntry();
            }
        }
        return baos.toByteArray();
    }

    private byte[] createEmptyZip() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            // No entries
        }
        return baos.toByteArray();
    }

    private byte[] createMinimalPng() {
        try {
            BufferedImage img = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
            img.setRGB(0, 0, 0xFF0000);
            img.setRGB(1, 0, 0x00FF00);
            img.setRGB(0, 1, 0x0000FF);
            img.setRGB(1, 1, 0xFFFFFF);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private byte[] createMinimalJpeg() {
        try {
            BufferedImage img = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
            img.setRGB(0, 0, 0xFF0000);
            img.setRGB(1, 1, 0x0000FF);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "jpg", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
