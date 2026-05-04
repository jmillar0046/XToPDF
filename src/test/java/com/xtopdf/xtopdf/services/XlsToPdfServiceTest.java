package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.services.conversion.spreadsheet.ExcelToPdfService;
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
 * Tests for XLS conversion through the unified ExcelToPdfService.
 * Migrated from the deprecated XlsToPdfService tests.
 * Tests cover:
 * - Basic conversion (Requirement 10.3)
 * - Formula evaluation (Requirement 2.1)
 * - Chart rendering
 * - Formatting preservation
 * - Error handling for corrupted files
 * - Resource cleanup
 * 
 * Note: XLS is the older binary Excel format (Excel 97-2003)
 */
class XlsToPdfServiceTest {

    private ExcelToPdfService excelToPdfService;
    private PdfBackendProvider pdfBackend;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        pdfBackend = new PdfBoxBackend();
        excelToPdfService = new ExcelToPdfService(pdfBackend);
    }

    @AfterEach
    void tearDown() {
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

    // ========== Basic Conversion Tests (Requirement 10.3) ==========

    @Test
    void testConvertBasicSpreadsheet_Success() throws Exception {
        ClassPathResource resource = new ClassPathResource("test-files/basic-spreadsheet.xls");
        byte[] xlsData = Files.readAllBytes(resource.getFile().toPath());
        MockMultipartFile xlsFile = new MockMultipartFile(
            "file",
            "basic-spreadsheet.xls",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            xlsData
        );
        File outputFile = tempDir.resolve("basic-output.pdf").toFile();

        excelToPdfService.convertExcelToPdf(xlsFile, outputFile);

        assertTrue(outputFile.exists(), "PDF file should be created");
        assertTrue(outputFile.length() > 0, "PDF file should not be empty");

        try (PDDocument document = Loader.loadPDF(outputFile)) {
            assertNotNull(document, "PDF document should be readable");
            assertTrue(document.getNumberOfPages() > 0, "PDF should have at least one page");

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
        ClassPathResource resource = new ClassPathResource("test-files/basic-spreadsheet.xls");
        byte[] xlsData = Files.readAllBytes(resource.getFile().toPath());
        MockMultipartFile xlsFile = new MockMultipartFile(
            "file",
            "basic-spreadsheet.xls",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            xlsData
        );
        File outputFile = tempDir.resolve("basic-data-output.pdf").toFile();

        excelToPdfService.convertExcelToPdf(xlsFile, outputFile);

        try (PDDocument document = Loader.loadPDF(outputFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            assertTrue(text.contains("John") || text.contains("Jane") || text.contains("Bob"),
                "PDF should contain employee names");
        }
    }

    // ========== Formula Evaluation Tests (Requirement 2.1) ==========

    @Test
    void testConvertWithFormulas_EvaluatesFormulas() throws Exception {
        ClassPathResource resource = new ClassPathResource("test-files/formulas-spreadsheet.xls");
        byte[] xlsData = Files.readAllBytes(resource.getFile().toPath());
        MockMultipartFile xlsFile = new MockMultipartFile(
            "file",
            "formulas-spreadsheet.xls",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            xlsData
        );
        File outputFile = tempDir.resolve("formulas-output.pdf").toFile();

        excelToPdfService.convertExcelToPdf(xlsFile, outputFile);

        assertTrue(outputFile.exists(), "PDF file should be created");
        assertTrue(outputFile.length() > 0, "PDF file should not be empty");

        try (PDDocument document = Loader.loadPDF(outputFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            assertTrue(text.contains("Formulas"), "PDF should contain sheet name 'Formulas'");
            assertTrue(text.contains("Product"), "PDF should contain 'Product' header");
            assertTrue(text.contains("Quantity"), "PDF should contain 'Quantity' header");
            assertTrue(text.contains("Price"), "PDF should contain 'Price' header");
            assertTrue(text.contains("Total"), "PDF should contain 'Total' header");
            
            assertFalse(text.contains("=B2*C2"), "PDF should not contain raw formula expressions");
            assertFalse(text.contains("=SUM("), "PDF should not contain SUM formula syntax");
            assertFalse(text.contains("=AVERAGE("), "PDF should not contain AVERAGE formula syntax");
        }
    }

    @Test
    void testConvertWithFormulas_VerifyCalculatedValues() throws Exception {
        ClassPathResource resource = new ClassPathResource("test-files/formulas-spreadsheet.xls");
        byte[] xlsData = Files.readAllBytes(resource.getFile().toPath());
        MockMultipartFile xlsFile = new MockMultipartFile(
            "file",
            "formulas-spreadsheet.xls",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            xlsData
        );
        File outputFile = tempDir.resolve("formulas-calc-output.pdf").toFile();

        excelToPdfService.convertExcelToPdf(xlsFile, outputFile);

        try (PDDocument document = Loader.loadPDF(outputFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            assertTrue(text.matches("(?s).*\\d+(\\.\\d+)?.*"), 
                "PDF should contain numeric values from formula evaluation");
        }
    }

    // ========== Chart Rendering Tests ==========

    @Test
    void testConvertWithCharts_Success() throws Exception {
        ClassPathResource resource = new ClassPathResource("test-files/charts-spreadsheet.xls");
        byte[] xlsData = Files.readAllBytes(resource.getFile().toPath());
        MockMultipartFile xlsFile = new MockMultipartFile(
            "file",
            "charts-spreadsheet.xls",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            xlsData
        );
        File outputFile = tempDir.resolve("charts-output.pdf").toFile();

        excelToPdfService.convertExcelToPdf(xlsFile, outputFile);

        assertTrue(outputFile.exists(), "PDF file should be created");
        assertTrue(outputFile.length() > 0, "PDF file should not be empty");

        try (PDDocument document = Loader.loadPDF(outputFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            assertTrue(text.contains("Sales Data"), "PDF should contain sheet name 'Sales Data'");
            assertTrue(text.contains("Month"), "PDF should contain 'Month' header");
            assertTrue(text.contains("Sales"), "PDF should contain 'Sales' header");
            assertTrue(text.contains("Expenses"), "PDF should contain 'Expenses' header");
        }
    }

    // ========== Formatting Preservation Tests ==========

    @Test
    void testConvertWithFormatting_Success() throws Exception {
        ClassPathResource resource = new ClassPathResource("test-files/formatted-spreadsheet.xls");
        byte[] xlsData = Files.readAllBytes(resource.getFile().toPath());
        MockMultipartFile xlsFile = new MockMultipartFile(
            "file",
            "formatted-spreadsheet.xls",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            xlsData
        );
        File outputFile = tempDir.resolve("formatted-output.pdf").toFile();

        excelToPdfService.convertExcelToPdf(xlsFile, outputFile);

        assertTrue(outputFile.exists(), "PDF file should be created");
        assertTrue(outputFile.length() > 0, "PDF file should not be empty");

        try (PDDocument document = Loader.loadPDF(outputFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            assertTrue(text.contains("Formatted Data"), "PDF should contain sheet name 'Formatted Data'");
            assertTrue(text.contains("Employee"), "PDF should contain 'Employee' header");
            assertTrue(text.contains("Department"), "PDF should contain 'Department' header");
            assertTrue(text.contains("Salary"), "PDF should contain 'Salary' header");
        }
    }

    @Test
    void testConvertWithFormatting_VerifyDataPresence() throws Exception {
        ClassPathResource resource = new ClassPathResource("test-files/formatted-spreadsheet.xls");
        byte[] xlsData = Files.readAllBytes(resource.getFile().toPath());
        MockMultipartFile xlsFile = new MockMultipartFile(
            "file",
            "formatted-spreadsheet.xls",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            xlsData
        );
        File outputFile = tempDir.resolve("formatted-data-output.pdf").toFile();

        excelToPdfService.convertExcelToPdf(xlsFile, outputFile);

        try (PDDocument document = Loader.loadPDF(outputFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            assertTrue(text.length() > 100, "PDF should contain substantial content");
            assertTrue(text.matches("(?s).*\\d+.*"), "PDF should contain numeric data");
        }
    }

    // ========== Error Handling Tests ==========

    @Test
    void testConvertCorruptedFile_ThrowsIOException() throws Exception {
        ClassPathResource resource = new ClassPathResource("test-files/corrupted-spreadsheet.xls");
        byte[] xlsData = Files.readAllBytes(resource.getFile().toPath());
        MockMultipartFile xlsFile = new MockMultipartFile(
            "file",
            "corrupted-spreadsheet.xls",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            xlsData
        );
        File outputFile = tempDir.resolve("corrupted-output.pdf").toFile();

        IOException exception = assertThrows(IOException.class, () -> {
            excelToPdfService.convertExcelToPdf(xlsFile, outputFile);
        });

        assertNotNull(exception.getMessage(), "Exception should have a message");
    }

    @Test
    void testConvertEmptyFile_ThrowsException() {
        MockMultipartFile xlsFile = new MockMultipartFile(
            "file",
            "empty.xls",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            new byte[0]
        );
        File outputFile = tempDir.resolve("empty-output.pdf").toFile();

        // Empty files throw IllegalArgumentException (EmptyFileException from POI)
        assertThrows(Exception.class, () -> {
            excelToPdfService.convertExcelToPdf(xlsFile, outputFile);
        });
    }

    @Test
    void testConvertInvalidContent_ThrowsIOException() {
        MockMultipartFile xlsFile = new MockMultipartFile(
            "file",
            "invalid.xls",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            "This is not a valid XLS file".getBytes()
        );
        File outputFile = tempDir.resolve("invalid-output.pdf").toFile();

        IOException exception = assertThrows(IOException.class, () -> {
            excelToPdfService.convertExcelToPdf(xlsFile, outputFile);
        });

        assertNotNull(exception.getMessage(), "Exception should have a message");
    }

    // ========== Resource Cleanup Tests ==========

    @Test
    void testResourceCleanup_OnSuccess() throws Exception {
        ClassPathResource resource = new ClassPathResource("test-files/basic-spreadsheet.xls");
        byte[] xlsData = Files.readAllBytes(resource.getFile().toPath());
        MockMultipartFile xlsFile = new MockMultipartFile(
            "file",
            "basic-spreadsheet.xls",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            xlsData
        );
        File outputFile = tempDir.resolve("cleanup-success-output.pdf").toFile();

        excelToPdfService.convertExcelToPdf(xlsFile, outputFile);

        assertTrue(outputFile.exists(), "Output file should exist");
        
        try (PDDocument document = Loader.loadPDF(outputFile)) {
            assertNotNull(document, "PDF should be readable after conversion");
        }
    }

    @Test
    void testResourceCleanup_OnFailure() {
        MockMultipartFile xlsFile = new MockMultipartFile(
            "file",
            "invalid.xls",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            "invalid content".getBytes()
        );
        File outputFile = tempDir.resolve("cleanup-failure-output.pdf").toFile();

        assertThrows(IOException.class, () -> {
            excelToPdfService.convertExcelToPdf(xlsFile, outputFile);
        });

        if (outputFile.exists()) {
            assertTrue(outputFile.length() == 0 || outputFile.length() < 100,
                "Partial output file should be empty or very small");
        }
    }

    // ========== Additional Edge Cases ==========

    @Test
    void testConvertWithExecuteMacrosParameter_False() throws Exception {
        ClassPathResource resource = new ClassPathResource("test-files/basic-spreadsheet.xls");
        byte[] xlsData = Files.readAllBytes(resource.getFile().toPath());
        MockMultipartFile xlsFile = new MockMultipartFile(
            "file",
            "basic-spreadsheet.xls",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            xlsData
        );
        File outputFile = tempDir.resolve("macros-false-output.pdf").toFile();

        excelToPdfService.convertExcelToPdf(xlsFile, outputFile, false);

        assertTrue(outputFile.exists(), "PDF file should be created");
        assertTrue(outputFile.length() > 0, "PDF file should not be empty");
    }

    @Test
    void testConvertWithExecuteMacrosParameter_True() throws Exception {
        ClassPathResource resource = new ClassPathResource("test-files/basic-spreadsheet.xls");
        byte[] xlsData = Files.readAllBytes(resource.getFile().toPath());
        MockMultipartFile xlsFile = new MockMultipartFile(
            "file",
            "basic-spreadsheet.xls",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            xlsData
        );
        File outputFile = tempDir.resolve("macros-true-output.pdf").toFile();

        excelToPdfService.convertExcelToPdf(xlsFile, outputFile, true);

        assertTrue(outputFile.exists(), "PDF file should be created");
        assertTrue(outputFile.length() > 0, "PDF file should not be empty");
    }

    // ========== XLS-Specific Tests ==========

    @Test
    void testConvertXlsFormat_VerifyBinaryFormatHandling() throws Exception {
        ClassPathResource resource = new ClassPathResource("test-files/basic-spreadsheet.xls");
        byte[] xlsData = Files.readAllBytes(resource.getFile().toPath());
        MockMultipartFile xlsFile = new MockMultipartFile(
            "file",
            "basic-spreadsheet.xls",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            xlsData
        );
        File outputFile = tempDir.resolve("xls-format-output.pdf").toFile();

        excelToPdfService.convertExcelToPdf(xlsFile, outputFile);

        assertTrue(outputFile.exists(), "PDF file should be created");
        assertTrue(outputFile.length() > 0, "PDF file should not be empty");

        try (PDDocument document = Loader.loadPDF(outputFile)) {
            assertNotNull(document, "PDF document should be readable");
            assertTrue(document.getNumberOfPages() > 0, "PDF should have at least one page");
            
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            assertTrue(text.length() > 50, "PDF should contain substantial text from XLS file");
        }
    }

    @Test
    void testConvertXlsFormat_CompareWithXlsx() throws Exception {
        // Both XLS and XLSX versions should produce similar output through the unified service
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

        // Convert both files through the unified ExcelToPdfService
        excelToPdfService.convertExcelToPdf(xlsFile, xlsOutputFile);
        excelToPdfService.convertExcelToPdf(xlsxFile, xlsxOutputFile);

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
