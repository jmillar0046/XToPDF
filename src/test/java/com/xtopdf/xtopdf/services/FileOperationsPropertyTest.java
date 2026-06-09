package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.services.operations.PdfMergeService;
import com.xtopdf.xtopdf.utils.PdfFileHelper;
import net.jqwik.api.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for file operations (PdfFileHelper, PdfMergeService).
 *
 * Verifies that temp file cleanup always occurs regardless of processing outcome.
 *
 * **Validates: Requirements 32.4**
 */
class FileOperationsPropertyTest {

    @Property(tries = 25)
    @Tag("Feature: advanced-improvements, Property: PdfFileHelper always cleans up temp files on success")
    void pdfFileHelperCleansUpTempFilesOnSuccess(
            @ForAll("pdfFilenames") String filename) throws IOException {

        byte[] pdfBytes = createMinimalPdf();
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file", filename, "application/pdf", pdfBytes);

        // Track temp files before the call
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        long tempFileCountBefore = countPdfTempFiles(tempDir);

        var response = PdfFileHelper.processPdfFile(pdfFile, file -> {
            // No-op processor — just a successful pass-through
        }, "output.pdf");

        assertThat(response.getStatusCode().value())
                .as("Successful processing should return 200")
                .isEqualTo(200);

        // Temp files should not accumulate
        long tempFileCountAfter = countPdfTempFiles(tempDir);
        assertThat(tempFileCountAfter)
                .as("Temp file count should not increase after processing")
                .isLessThanOrEqualTo(tempFileCountBefore);
    }

    @Property(tries = 25)
    @Tag("Feature: advanced-improvements, Property: PdfFileHelper cleans up temp files on IOException")
    void pdfFileHelperCleansUpTempFilesOnError(
            @ForAll("errorMessages") String errorMessage) throws IOException {

        byte[] pdfBytes = createMinimalPdf();
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", pdfBytes);

        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        long tempFileCountBefore = countPdfTempFiles(tempDir);

        var response = PdfFileHelper.processPdfFile(pdfFile, file -> {
            throw new IOException(errorMessage);
        }, "output.pdf");

        assertThat(response.getStatusCode().value())
                .as("IOException should result in HTTP 500")
                .isEqualTo(500);

        // Temp files should still be cleaned up even on error
        long tempFileCountAfter = countPdfTempFiles(tempDir);
        assertThat(tempFileCountAfter)
                .as("Temp file count should not increase after error")
                .isLessThanOrEqualTo(tempFileCountBefore);
    }

    @Property(tries = 25)
    @Tag("Feature: advanced-improvements, Property: PdfMergeService cleans up temp files after merge")
    void pdfMergeServiceCleansUpTempFiles(
            @ForAll("mergePositions") String position) throws IOException {

        PdfMergeService mergeService = new PdfMergeService();

        byte[] pdfBytes = createMinimalPdf();
        MockMultipartFile existingPdf = new MockMultipartFile(
                "file", "existing.pdf", "application/pdf", pdfBytes);

        // Create a real PDF file for the converted PDF
        File convertedPdf = Files.createTempFile("converted_", ".pdf").toFile();
        try (FileOutputStream fos = new FileOutputStream(convertedPdf)) {
            fos.write(pdfBytes);
        }

        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        long tempFileCountBefore = countPdfTempFiles(tempDir);

        try {
            mergeService.mergePdfs(convertedPdf, existingPdf, position);

            // Verify merged PDF is valid
            assertThat(convertedPdf).exists();
            assertThat(convertedPdf.length()).isGreaterThan(0);
        } finally {
            // Cleanup should have removed temp files (existing_ and merged_)
            long tempFileCountAfter = countPdfTempFiles(tempDir);
            // The only remaining temp file should be our convertedPdf
            assertThat(tempFileCountAfter)
                    .as("Temp files from merge should be cleaned up")
                    .isLessThanOrEqualTo(tempFileCountBefore + 1);

            convertedPdf.delete();
        }
    }

    // ---------------------------------------------------------------
    // Generators
    // ---------------------------------------------------------------

    @Provide
    Arbitrary<String> pdfFilenames() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(1)
                .ofMaxLength(20)
                .map(s -> s + ".pdf");
    }

    @Provide
    Arbitrary<String> errorMessages() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(1)
                .ofMaxLength(30)
                .map(s -> "IO error: " + s);
    }

    @Provide
    Arbitrary<String> mergePositions() {
        return Arbitraries.of("front", "back");
    }

    // ---------------------------------------------------------------
    // Helper Methods
    // ---------------------------------------------------------------

    private byte[] createMinimalPdf() throws IOException {
        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage(PDRectangle.A4));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();
        }
    }

    private long countPdfTempFiles(File tempDir) {
        File[] files = tempDir.listFiles((dir, name) ->
                name.startsWith("pdf_") || name.startsWith("merged_") || name.startsWith("existing_"));
        return files != null ? files.length : 0;
    }
}
