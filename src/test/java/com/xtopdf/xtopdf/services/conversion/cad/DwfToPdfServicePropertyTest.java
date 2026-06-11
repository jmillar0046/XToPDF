package com.xtopdf.xtopdf.services.conversion.cad;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.springframework.mock.web.MockMultipartFile;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for the DWF renderer.
 * Validates the fallback guarantee and entry classification properties.
 */
@Tag("Feature: cad-converter-rendering, Properties 6: DWF renderer")
class DwfToPdfServicePropertyTest {

    /**
     * Property 6: Fallback Guarantee
     *
     * For any DWF file (ZIP archive) containing no image entries, the converter
     * SHALL produce a valid PDF with statistics content and SHALL NOT throw an exception.
     */
    @Property(tries = 25)
    @Label("Random ZIP with no images produces statistics without exception")
    void fallbackGuarantee(
            @ForAll @IntRange(min = 0, max = 8) int entryCount,
            @ForAll("nonImageExtensions") String extension) throws Exception {

        PdfBackendProvider mockProvider = mock(PdfBackendProvider.class);
        PdfDocumentBuilder mockBuilder = mock(PdfDocumentBuilder.class);
        when(mockProvider.createBuilder()).thenReturn(mockBuilder);
        DwfToPdfService service = new DwfToPdfService(mockProvider);

        // Build a ZIP with non-image entries only
        byte[] zipBytes = createZipWithNonImageEntries(entryCount, extension);

        MockMultipartFile file = new MockMultipartFile(
            "file", "test.dwf", "application/octet-stream", zipBytes);
        File pdfFile = File.createTempFile("dwf-prop-", ".pdf");
        pdfFile.deleteOnExit();

        // Should not throw
        assertThatNoException().isThrownBy(() -> service.convertDwfToPdf(file, pdfFile));

        // Should render statistics (newPage called for statistics page)
        verify(mockBuilder).newPage(595f, 842f);
        // Should not embed images
        verify(mockBuilder, never()).addImage(any(byte[].class));
        // Should save
        verify(mockBuilder).save(pdfFile);
    }

    /**
     * Property: Entry Classification
     *
     * For any filename, classifyEntry always returns one of the valid classification types.
     */
    @Property(tries = 25)
    @Label("Random filenames always get a valid classification type")
    void entryClassification(
            @ForAll("randomFilenames") String filename) {

        DwfToPdfService service = new DwfToPdfService(mock(PdfBackendProvider.class));

        String result = service.classifyEntry(filename.toLowerCase());

        assertThat(result).as("Classification for '%s'", filename)
            .isIn("Drawing (W2D)", "Image", "XML Metadata", "Font", "Other");
    }

    // --- Providers ---

    @Provide
    Arbitrary<String> nonImageExtensions() {
        return Arbitraries.of(".w2d", ".xml", ".ttf", ".otf", ".dat", ".bin", ".txt", ".dwf");
    }

    @Provide
    Arbitrary<String> randomFilenames() {
        Arbitrary<String> baseName = Arbitraries.strings()
            .alpha()
            .ofMinLength(1)
            .ofMaxLength(20);

        Arbitrary<String> extension = Arbitraries.of(
            ".w2d", ".png", ".jpg", ".jpeg", ".bmp", ".tiff", ".tif",
            ".xml", ".ttf", ".otf", ".dat", ".bin", ".txt", ".svg",
            ".html", ".css", ".json", ".dwf", ".dxf", ""
        );

        return Combinators.combine(baseName, extension).as((name, ext) -> name + ext);
    }

    // --- Helpers ---

    private byte[] createZipWithNonImageEntries(int count, String extension) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (int i = 0; i < count; i++) {
                String entryName = "entry_" + i + extension;
                zos.putNextEntry(new ZipEntry(entryName));
                // Write some arbitrary non-image data
                zos.write(("content-" + i).getBytes());
                zos.closeEntry();
            }
        }
        return baos.toByteArray();
    }
}
