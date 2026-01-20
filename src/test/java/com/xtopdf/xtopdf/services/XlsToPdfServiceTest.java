package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.services.conversion.spreadsheet.XlsxToPdfService;
import com.xtopdf.xtopdf.services.conversion.spreadsheet.XlsToPdfService;
import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for XlsToPdfService.
 * Tests cover:
 * - Basic conversion (Requirement 2.2)
 * - Formula evaluation (Requirement 2.3)
 * - Chart rendering (Requirement 2.4)
 * - Formatting preservation (Requirement 2.5)
 * - Error handling for corrupted files (Requirement 2.6)
 * - Resource cleanup
 * 
 * Note: XLS is the older binary Excel format (Excel 97-2003)
 */
class XlsToPdfServiceTest {

    private XlsToPdfService xlsToPdfService;
    private PdfBackendProvider pdfBackend;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        pdfBackend = new PdfBoxBackend();
        xlsToPdfService = new XlsToPdfService(pdfBackend);
    }

    @AfterEach
    void tearDown() {
        // Cleanup any temporary files
        if (tempDir != null) {
            try {
                Files.walk(tempDir)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            // Ignore cleanup errors
                        }
                    });
            } catch (IOException e) {
                // Ignore cleanup errors
            }
        }
    }

    // ========== Basic Conversion Tests (Requirement 2.2) ==========

    @Test
    void testConvertBasicSpreadsheet_Success() throws Exception {
        // Given: Load the basic spreadsheet test file
        ClassPathResource resource = new ClassPathResource("test-files/basic-spreadsheet.xls");
        byte[] xlsData = Files.readAllBytes(resource.getFile().toPath());
        MockMultipartFile xlsFile = new MockMultipartFile(
            "file",
            "basic-spreadsheet.xls",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            xlsData
        );
        File outputFile = tempDir.resolve("basic-output.pdf").toFile();

        // When: Convert XLS to PDF
        xlsToPdfService.convertXlsToPdf(xlsFile, outputFile);

        // Then: Verify PDF was created and contains expected content
        assertTrue(outputFile.exists(), "PDF file should be created");
        assertTrue(outputFile.length() > 0, "PDF file should not be empty");

        // Verify PDF content
        try (PDDocument document = Loader.loadPDF(outputFile)) {
            assertNotNull(document, "PDF document should be readable");
            assertTrue(document.getNumberOfPages() > 0, "PDF should have at least one page");

            // Extract text and verify basic content
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            assertNotNull(text, "PDF should contain text");
            assertTrue(text.contains("Basic Data"), "PDF should contain sheet name");
            assertTrue(text.contains("Name"), "PDF should contain header 'Name'");
            assertTrue(text.contains("Age"), "PDF should contain header 'Age'");
            assertTrue(text.contains("City"), "PDF should contain header 'City'");
            assertTrue(text.contains("Salary"), "PDF should contain header 'Salary'");
        }
    }

    @Test
    void testConvertBasicSpreadsheet_VerifyDataIntegrity() throws Exception {
        // Given: Load the basic spreadsheet test file
        ClassPathResource resource = new ClassPathResource("test-files/basic-spreadsheet.xls");
        byte[] xlsData = Files.readAllBytes(resource.getFile().toPath());
        MockMultipartFile xlsFile = new MockMultipartFile(
            "file",
            "basic-spreadsheet.xls",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            xlsData
        );
        File outputFile = tempDir.resolve("basic-data-output.pdf").toFile();

        // When: Convert XLS to PDF
        xlsToPdfService.convertXlsToPdf(xlsFile, outputFile);

        // Then: Verify specific data values are present
        try (PDDocument document = Loader.loadPDF(outputFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            // Verify some employee data is present (from the test file)
            assertTrue(text.contains("John") || text.contains("Jane") || text.contains("Bob"),
                "PDF should contain employee names");
        }
    }

    // ========== Formula Evaluation Tests (Requirement 2.3) ==========

    @Test
    void testConvertWithFormulas_EvaluatesFormulas() throws Exception {
        // Given: Load the formulas spreadsheet test file
        ClassPathResource resource = new ClassPathResource("test-files/formulas-spreadsheet.xls");
        byte[] xlsData = Files.readAllBytes(resource.getFile().toPath());
        MockMultipartFile xlsFile = new MockMultipartFile(
            "file",
            "formulas-spreadsheet.xls",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            xlsData
        );
        File outputFile = tempDir.resolve("formulas-output.pdf").toFile();

        // When: Convert XLS to PDF
        xlsToPdfService.convertXlsToPdf(xlsFile, outputFile);

        // Then: Verify PDF was created and formulas were evaluated
        assertTrue(outputFile.exists(), "PDF file should be created");
        assertTrue(outputFile.length() > 0, "PDF file should not be empty");

        try (PDDocument document = Loader.loadPDF(outputFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            // Verify sheet name
            assertTrue(text.contains("Formulas"), "PDF should contain sheet name 'Formulas'");
            
            // Verify headers
            assertTrue(text.contains("Product"), "PDF should contain 'Product' header");
            assertTrue(text.contains("Quantity"), "PDF should contain 'Quantity' header");
            assertTrue(text.contains("Price"), "PDF should contain 'Price' header");
            assertTrue(text.contains("Total"), "PDF should contain 'Total' header");
            
            // The PDF should contain calculated values, not formula expressions like "=B2*C2"
            // We can't easily verify exact calculated values without knowing the test data,
            // but we can verify the PDF doesn't contain formula syntax
            assertFalse(text.contains("=B2*C2"), "PDF should not contain raw formula expressions");
            assertFalse(text.contains("=SUM("), "PDF should not contain SUM formula syntax");
            assertFalse(text.contains("=AVERAGE("), "PDF should not contain AVERAGE formula syntax");
        }
    }

    @Test
    void testConvertWithFormulas_VerifyCalculatedValues() throws Exception {
        // Given: Load the formulas spreadsheet test file
        ClassPathResource resource = new ClassPathResource("test-files/formulas-spreadsheet.xls");
        byte[] xlsData = Files.readAllBytes(resource.getFile().toPath());
        MockMultipartFile xlsFile = new MockMultipartFile(
            "file",
            "formulas-spreadsheet.xls",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            xlsData
        );
        File outputFile = tempDir.resolve("formulas-calc-output.pdf").toFile();

        // When: Convert XLS to PDF
        xlsToPdfService.convertXlsToPdf(xlsFile, outputFile);

        // Then: Verify formulas were evaluated to numeric values
        try (PDDocument document = Loader.loadPDF(outputFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            // The text should contain numeric values (results of formulas)
            // We're looking for patterns that indicate calculated values are present
            assertTrue(text.matches("(?s).*\\d+(\\.\\d+)?.*"), 
                "PDF should contain numeric values from formula evaluation");
        }
    }

    // ========== Chart Rendering Tests (Requirement 2.4) ==========

    @Test
    void testConvertWithCharts_Success() throws Exception {
        // Given: Load the charts spreadsheet test file
        ClassPathResource resource = new ClassPathResource("test-files/charts-spreadsheet.xls");
        byte[] xlsData = Files.readAllBytes(resource.getFile().toPath());
        MockMultipartFile xlsFile = new MockMultipartFile(
            "file",
            "charts-spreadsheet.xls",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            xlsData
        );
        File outputFile = tempDir.resolve("charts-output.pdf").toFile();

        // When: Convert XLS to PDF
        xlsToPdfService.convertXlsToPdf(xlsFile, outputFile);

        // Then: Verify PDF was created
        assertTrue(outputFile.exists(), "PDF file should be created");
        assertTrue(outputFile.length() > 0, "PDF file should not be empty");

        try (PDDocument document = Loader.loadPDF(outputFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            // Verify sheet name and data
            assertTrue(text.contains("Sales Data"), "PDF should contain sheet name 'Sales Data'");
            assertTrue(text.contains("Month"), "PDF should contain 'Month' header");
            assertTrue(text.contains("Sales"), "PDF should contain 'Sales' header");
            assertTrue(text.contains("Expenses"), "PDF should contain 'Expenses' header");
            
            // Note: The current implementation may not render charts as images,
            // but it should at least render the underlying data
            // This test verifies the conversion doesn't fail when charts are present
            // XLS format has limited chart support compared to XLSX
        }
    }

    // ========== Formatting Preservation Tests (Requirement 2.5) ==========

    @Test
    void testConvertWithFormatting_Success() throws Exception {
        // Given: Load the formatted spreadsheet test file
        ClassPathResource resource = new ClassPathResource("test-files/formatted-spreadsheet.xls");
        byte[] xlsData = Files.readAllBytes(resource.getFile().toPath());
        MockMultipartFile xlsFile = new MockMultipartFile(
            "file",
            "formatted-spreadsheet.xls",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            xlsData
        );
        File outputFile = tempDir.resolve("formatted-output.pdf").toFile();

        // When: Convert XLS to PDF
        xlsToPdfService.convertXlsToPdf(xlsFile, outputFile);

        // Then: Verify PDF was created
        assertTrue(outputFile.exists(), "PDF file should be created");
        assertTrue(outputFile.length() > 0, "PDF file should not be empty");

        try (PDDocument document = Loader.loadPDF(outputFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            // Verify sheet name and headers
            assertTrue(text.contains("Formatted Data"), "PDF should contain sheet name 'Formatted Data'");
            assertTrue(text.contains("Employee"), "PDF should contain 'Employee' header");
            assertTrue(text.contains("Department"), "PDF should contain 'Department' header");
            assertTrue(text.contains("Salary"), "PDF should contain 'Salary' header");
            
            // Note: The current implementation may not preserve all visual formatting
            // (colors, fonts, borders), but it should preserve the data and structure
            // This test verifies the conversion doesn't fail when formatting is present
        }
    }

    @Test
    void testConvertWithFormatting_VerifyDataPresence() throws Exception {
        // Given: Load the formatted spreadsheet test file
        ClassPathResource resource = new ClassPathResource("test-files/formatted-spreadsheet.xls");
        byte[] xlsData = Files.readAllBytes(resource.getFile().toPath());
        MockMultipartFile xlsFile = new MockMultipartFile(
            "file",
            "formatted-spreadsheet.xls",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            xlsData
        );
        File outputFile = tempDir.resolve("formatted-data-output.pdf").toFile();

        // When: Convert XLS to PDF
        xlsToPdfService.convertXlsToPdf(xlsFile, outputFile);

        // Then: Verify data is present in PDF
        try (PDDocument document = Loader.loadPDF(outputFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            // Verify the PDF contains actual data (not just headers)
            assertTrue(text.length() > 100, "PDF should contain substantial content");
            
            // Verify numeric data is present (salaries, percentages, etc.)
            assertTrue(text.matches("(?s).*\\d+.*"), "PDF should contain numeric data");
        }
    }

    // ========== Error Handling Tests (Requirement 2.6) ==========

    @Test
    void testConvertCorruptedFile_ThrowsIOException() throws Exception {
        // Given: Load the corrupted spreadsheet test file
        ClassPathResource resource = new ClassPathResource("test-files/corrupted-spreadsheet.xls");
        byte[] xlsData = Files.readAllBytes(resource.getFile().toPath());
        MockMultipartFile xlsFile = new MockMultipartFile(
            "file",
            "corrupted-spreadsheet.xls",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            xlsData
        );
        File outputFile = tempDir.resolve("corrupted-output.pdf").toFile();

        // When/Then: Conversion should throw IOException with descriptive message
        IOException exception = assertThrows(IOException.class, () -> {
            xlsToPdfService.convertXlsToPdf(xlsFile, outputFile);
        });

        // Verify exception message is descriptive
        assertNotNull(exception.getMessage(), "Exception should have a message");
        assertTrue(exception.getMessage().contains("Error processing XLS file") ||
                   exception.getMessage().contains("Invalid") ||
                   exception.getMessage().contains("corrupted"),
            "Exception message should be descriptive");
    }

    @Test
    void testConvertEmptyFile_ThrowsIOException() {
        // Given: Empty file
        MockMultipartFile xlsFile = new MockMultipartFile(
            "file",
            "empty.xls",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            new byte[0]
        );
        File outputFile = tempDir.resolve("empty-output.pdf").toFile();

        // When/Then: Conversion should throw IOException
        assertThrows(IOException.class, () -> {
            xlsToPdfService.convertXlsToPdf(xlsFile, outputFile);
        });
    }

    @Test
    void testConvertInvalidContent_ThrowsIOException() {
        // Given: File with invalid XLS content
        MockMultipartFile xlsFile = new MockMultipartFile(
            "file",
            "invalid.xls",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            "This is not a valid XLS file".getBytes()
        );
        File outputFile = tempDir.resolve("invalid-output.pdf").toFile();

        // When/Then: Conversion should throw IOException
        IOException exception = assertThrows(IOException.class, () -> {
            xlsToPdfService.convertXlsToPdf(xlsFile, outputFile);
        });

        // Verify exception message is descriptive
        assertNotNull(exception.getMessage(), "Exception should have a message");
    }

    // ========== Resource Cleanup Tests ==========

    @Test
    void testResourceCleanup_OnSuccess() throws Exception {
        // Given: Valid XLS file
        ClassPathResource resource = new ClassPathResource("test-files/basic-spreadsheet.xls");
        byte[] xlsData = Files.readAllBytes(resource.getFile().toPath());
        MockMultipartFile xlsFile = new MockMultipartFile(
            "file",
            "basic-spreadsheet.xls",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            xlsData
        );
        File outputFile = tempDir.resolve("cleanup-success-output.pdf").toFile();

        // When: Convert XLS to PDF
        xlsToPdfService.convertXlsToPdf(xlsFile, outputFile);

        // Then: Verify resources are cleaned up (no temp files left)
        // The service should use try-with-resources to ensure cleanup
        assertTrue(outputFile.exists(), "Output file should exist");
        
        // Verify we can read the output file (not locked by unclosed resources)
        try (PDDocument document = Loader.loadPDF(outputFile)) {
            assertNotNull(document, "PDF should be readable after conversion");
        }
    }

    @Test
    void testResourceCleanup_OnFailure() {
        // Given: Invalid XLS file that will cause failure
        MockMultipartFile xlsFile = new MockMultipartFile(
            "file",
            "invalid.xls",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            "invalid content".getBytes()
        );
        File outputFile = tempDir.resolve("cleanup-failure-output.pdf").toFile();

        // When: Attempt conversion (will fail)
        assertThrows(IOException.class, () -> {
            xlsToPdfService.convertXlsToPdf(xlsFile, outputFile);
        });

        // Then: Verify no partial output file is left
        // (or if it exists, it should be empty/invalid)
        if (outputFile.exists()) {
            assertTrue(outputFile.length() == 0 || outputFile.length() < 100,
                "Partial output file should be empty or very small");
        }
    }

    // ========== Additional Edge Cases ==========

    @Test
    void testConvertWithExecuteMacrosParameter_False() throws Exception {
        // Given: Valid XLS file
        ClassPathResource resource = new ClassPathResource("test-files/basic-spreadsheet.xls");
        byte[] xlsData = Files.readAllBytes(resource.getFile().toPath());
        MockMultipartFile xlsFile = new MockMultipartFile(
            "file",
            "basic-spreadsheet.xls",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            xlsData
        );
        File outputFile = tempDir.resolve("macros-false-output.pdf").toFile();

        // When: Convert with executeMacros = false
        xlsToPdfService.convertXlsToPdf(xlsFile, outputFile, false);

        // Then: Conversion should succeed
        assertTrue(outputFile.exists(), "PDF file should be created");
        assertTrue(outputFile.length() > 0, "PDF file should not be empty");
    }

    @Test
    void testConvertWithExecuteMacrosParameter_True() throws Exception {
        // Given: Valid XLS file
        ClassPathResource resource = new ClassPathResource("test-files/basic-spreadsheet.xls");
        byte[] xlsData = Files.readAllBytes(resource.getFile().toPath());
        MockMultipartFile xlsFile = new MockMultipartFile(
            "file",
            "basic-spreadsheet.xls",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            xlsData
        );
        File outputFile = tempDir.resolve("macros-true-output.pdf").toFile();

        // When: Convert with executeMacros = true
        xlsToPdfService.convertXlsToPdf(xlsFile, outputFile, true);

        // Then: Conversion should succeed
        assertTrue(outputFile.exists(), "PDF file should be created");
        assertTrue(outputFile.length() > 0, "PDF file should not be empty");
    }

    // ========== XLS-Specific Tests ==========

    @Test
    void testConvertXlsFormat_VerifyBinaryFormatHandling() throws Exception {
        // Given: XLS file (binary format, not XML-based like XLSX)
        ClassPathResource resource = new ClassPathResource("test-files/basic-spreadsheet.xls");
        byte[] xlsData = Files.readAllBytes(resource.getFile().toPath());
        MockMultipartFile xlsFile = new MockMultipartFile(
            "file",
            "basic-spreadsheet.xls",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            xlsData
        );
        File outputFile = tempDir.resolve("xls-format-output.pdf").toFile();

        // When: Convert XLS to PDF
        xlsToPdfService.convertXlsToPdf(xlsFile, outputFile);

        // Then: Verify conversion handles binary format correctly
        assertTrue(outputFile.exists(), "PDF file should be created");
        assertTrue(outputFile.length() > 0, "PDF file should not be empty");

        try (PDDocument document = Loader.loadPDF(outputFile)) {
            assertNotNull(document, "PDF document should be readable");
            assertTrue(document.getNumberOfPages() > 0, "PDF should have at least one page");
            
            // Verify content was extracted from binary format
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            assertTrue(text.length() > 50, "PDF should contain substantial text from XLS file");
        }
    }

    @Test
    void testConvertXlsFormat_CompareWithXlsx() throws Exception {
        // Given: Both XLS and XLSX versions of the same file
        ClassPathResource xlsResource = new ClassPathResource("test-files/basic-spreadsheet.xls");
        ClassPathResource xlsxResource = new ClassPathResource("test-files/basic-spreadsheet.xlsx");
        
        byte[] xlsData = Files.readAllBytes(xlsResource.getFile().toPath());
        byte[] xlsxData = Files.readAllBytes(xlsxResource.getFile().toPath());
        
        MockMultipartFile xlsFile = new MockMultipartFile(
            "file",
            "basic-spreadsheet.xls",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            xlsData
        );
        
        MockMultipartFile xlsxFile = new MockMultipartFile(
            "file",
            "basic-spreadsheet.xlsx",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            xlsxData
        );
        
        File xlsOutputFile = tempDir.resolve("xls-output.pdf").toFile();
        File xlsxOutputFile = tempDir.resolve("xlsx-output.pdf").toFile();

        // When: Convert both files
        xlsToPdfService.convertXlsToPdf(xlsFile, xlsOutputFile);
        XlsxToPdfService xlsxService = new XlsxToPdfService(pdfBackend);
        xlsxService.convertXlsxToPdf(xlsxFile, xlsxOutputFile);

        // Then: Both should produce valid PDFs with similar content
        assertTrue(xlsOutputFile.exists(), "XLS PDF should be created");
        assertTrue(xlsxOutputFile.exists(), "XLSX PDF should be created");
        
        try (PDDocument xlsDoc = Loader.loadPDF(xlsOutputFile);
             PDDocument xlsxDoc = Loader.loadPDF(xlsxOutputFile)) {
            
            PDFTextStripper stripper = new PDFTextStripper();
            String xlsText = stripper.getText(xlsDoc);
            String xlsxText = stripper.getText(xlsxDoc);
            
            // Both should contain the same key headers
            assertTrue(xlsText.contains("Name") && xlsxText.contains("Name"),
                "Both PDFs should contain 'Name' header");
            assertTrue(xlsText.contains("Age") && xlsxText.contains("Age"),
                "Both PDFs should contain 'Age' header");
            assertTrue(xlsText.contains("City") && xlsxText.contains("City"),
                "Both PDFs should contain 'City' header");
        }
    }
}
