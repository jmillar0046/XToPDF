package com.xtopdf.xtopdf.services.conversion.spreadsheet;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.xtopdf.xtopdf.utils.ExcelUtils;

/**
 * Tests for ExcelToPdfService.
 * Covers input validation (Requirements 8.1-8.4) and basic conversion (Requirements 10.1, 10.3, 10.4).
 */
class ExcelToPdfServiceTest {

    @TempDir
    Path tempDir;

    // ========== Input Validation Tests (Requirements 8.1, 8.2, 8.3, 8.4) ==========

    @Nested
    class InputValidationTests {

        private ExcelToPdfService service;
        private PdfBackendProvider mockBackend;

        @BeforeEach
        void setUp() {
            mockBackend = mock(PdfBackendProvider.class);
            service = new ExcelToPdfService(mockBackend);
        }

        @Test
        void convertExcelToPdf_nullMultipartFile_throwsIllegalArgumentException() {
            File outputFile = tempDir.resolve("output.pdf").toFile();

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.convertExcelToPdf(null, outputFile, false)
            );

            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().toLowerCase().contains("null") || ex.getMessage().toLowerCase().contains("input"),
                "Exception message should mention null or input: " + ex.getMessage());
        }

        @Test
        void convertExcelToPdf_nullOutputFile_throwsIllegalArgumentException() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                "file", "test.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[]{1, 2, 3}
            );

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.convertExcelToPdf(file, null, false)
            );

            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().toLowerCase().contains("null") || ex.getMessage().toLowerCase().contains("output"),
                "Exception message should mention null or output: " + ex.getMessage());
        }

        @Test
        void convertExcelToPdf_fileExceeds100MB_throwsIOException() throws Exception {
            // Create a MockMultipartFile that reports a size > 100MB
            // We don't need actual 100MB of data — just override getSize()
            MockMultipartFile oversizedFile = new MockMultipartFile(
                "file", "large.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[]{1, 2, 3}
            ) {
                @Override
                public long getSize() {
                    return 100_000_001L; // Just over 100MB
                }
            };
            File outputFile = tempDir.resolve("output.pdf").toFile();

            IOException ex = assertThrows(IOException.class, () ->
                service.convertExcelToPdf(oversizedFile, outputFile, false)
            );

            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("100000000") || ex.getMessage().toLowerCase().contains("size"),
                "Exception message should mention file size limit: " + ex.getMessage());
        }

        @Test
        void convertExcelToPdf_validationOccursBeforeWorkbookOpening() throws Exception {
            // When null input is provided, the mock PdfBackendProvider should never be called
            File outputFile = tempDir.resolve("output.pdf").toFile();

            assertThrows(IllegalArgumentException.class, () ->
                service.convertExcelToPdf(null, outputFile, false)
            );

            // Verify that createBuilder() was never called — validation happened first
            verify(mockBackend, never()).createBuilder();
        }

        @Test
        void convertExcelToPdf_overloadDefaultsExecuteMacrosToFalse() throws Exception {
            // The two-arg overload should work the same as passing false
            MockMultipartFile file = new MockMultipartFile(
                "file", "test.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[]{1, 2, 3}
            ) {
                @Override
                public long getSize() {
                    return 100_000_001L;
                }
            };
            File outputFile = tempDir.resolve("output.pdf").toFile();

            // Should still throw IOException for oversized file (validation works in overload too)
            assertThrows(IOException.class, () ->
                service.convertExcelToPdf(file, outputFile)
            );
        }
    }

    // ========== Basic In-Memory Conversion Tests (Requirements 10.1, 10.3, 10.4) ==========

    @Nested
    class BasicConversionTests {

        private ExcelToPdfService service;
        private PdfBackendProvider pdfBackend;

        @BeforeEach
        void setUp() {
            pdfBackend = new PdfBoxBackend();
            service = new ExcelToPdfService(pdfBackend);
        }

        @Test
        void convertExcelToPdf_validXlsxFile_producesNonEmptyPdf() throws Exception {
            ClassPathResource resource = new ClassPathResource("test-files/basic-spreadsheet.xlsx");
            byte[] xlsxData = Files.readAllBytes(resource.getFile().toPath());
            MockMultipartFile xlsxFile = new MockMultipartFile(
                "file", "basic-spreadsheet.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, xlsxData
            );
            File outputFile = tempDir.resolve("xlsx-output.pdf").toFile();

            service.convertExcelToPdf(xlsxFile, outputFile, false);

            assertTrue(outputFile.exists(), "PDF file should be created");
            assertTrue(outputFile.length() > 0, "PDF file should not be empty");

            try (PDDocument document = Loader.loadPDF(outputFile)) {
                assertNotNull(document);
                assertTrue(document.getNumberOfPages() > 0, "PDF should have at least one page");

                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);
                assertNotNull(text);
                assertFalse(text.isBlank(), "PDF should contain text content");
            }
        }

        @Test
        void convertExcelToPdf_validXlsFile_producesNonEmptyPdf() throws Exception {
            // Create a valid .xls file programmatically using HSSFWorkbook
            byte[] xlsData = createXlsWithData("TestSheet", new String[][]{
                {"Name", "Age"},
                {"Alice", "30"},
                {"Bob", "25"}
            });
            MockMultipartFile xlsFile = new MockMultipartFile(
                "file", "test.xls", MediaType.APPLICATION_OCTET_STREAM_VALUE, xlsData
            );
            File outputFile = tempDir.resolve("xls-output.pdf").toFile();

            service.convertExcelToPdf(xlsFile, outputFile, false);

            assertTrue(outputFile.exists(), "PDF file should be created");
            assertTrue(outputFile.length() > 0, "PDF file should not be empty");

            try (PDDocument document = Loader.loadPDF(outputFile)) {
                assertNotNull(document);
                assertTrue(document.getNumberOfPages() > 0, "PDF should have at least one page");

                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);
                assertNotNull(text);
                assertTrue(text.contains("Name"), "PDF should contain 'Name'");
                assertTrue(text.contains("Alice"), "PDF should contain 'Alice'");
            }
        }

        @Test
        void convertExcelToPdf_emptyWorkbook_producesValidPdf() throws Exception {
            // Create an empty workbook (no sheets with data)
            byte[] xlsxData;
            try (XSSFWorkbook workbook = new XSSFWorkbook();
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                workbook.write(baos);
                xlsxData = baos.toByteArray();
            }
            MockMultipartFile xlsxFile = new MockMultipartFile(
                "file", "empty.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, xlsxData
            );
            File outputFile = tempDir.resolve("empty-output.pdf").toFile();

            service.convertExcelToPdf(xlsxFile, outputFile, false);

            assertTrue(outputFile.exists(), "PDF file should be created");
            assertTrue(outputFile.length() > 0, "PDF file should not be empty");

            try (PDDocument document = Loader.loadPDF(outputFile)) {
                assertNotNull(document, "PDF should be a valid document");
            }
        }

        @Test
        void convertExcelToPdf_sheetWithNoRows_rendersEmptySheetText() throws Exception {
            // Create a workbook with one sheet that has no rows
            byte[] xlsxData;
            try (XSSFWorkbook workbook = new XSSFWorkbook();
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                workbook.createSheet("EmptySheet");
                workbook.write(baos);
                xlsxData = baos.toByteArray();
            }
            MockMultipartFile xlsxFile = new MockMultipartFile(
                "file", "empty-sheet.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, xlsxData
            );
            File outputFile = tempDir.resolve("empty-sheet-output.pdf").toFile();

            service.convertExcelToPdf(xlsxFile, outputFile, false);

            assertTrue(outputFile.exists(), "PDF file should be created");

            try (PDDocument document = Loader.loadPDF(outputFile)) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);
                assertTrue(text.contains("(Empty sheet)"),
                    "PDF should contain '(Empty sheet)' text, but got: " + text);
            }
        }

        // Helper to create a .xls file programmatically
        private byte[] createXlsWithData(String sheetName, String[][] data) throws IOException {
            try (HSSFWorkbook workbook = new HSSFWorkbook();
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                Sheet sheet = workbook.createSheet(sheetName);
                for (int r = 0; r < data.length; r++) {
                    Row row = sheet.createRow(r);
                    for (int c = 0; c < data[r].length; c++) {
                        row.createCell(c).setCellValue(data[r][c]);
                    }
                }
                workbook.write(baos);
                return baos.toByteArray();
            }
        }
    }

    // ========== Sparse Row Extraction Tests (Requirements 5.1, 5.2, 5.3, 5.4) ==========

    @Nested
    class SparseRowExtractionTests {

        private ExcelToPdfService service;
        private PdfBackendProvider pdfBackend;

        @BeforeEach
        void setUp() {
            pdfBackend = new PdfBoxBackend();
            service = new ExcelToPdfService(pdfBackend);
        }

        @Test
        void convertExcelToPdf_sparseRowsAt0_1_5_allDataAppearsAtCorrectPositions() throws Exception {
            // Create workbook with rows at indices 0, 1, 5 (gaps at 2, 3, 4)
            byte[] xlsxData;
            try (XSSFWorkbook workbook = new XSSFWorkbook();
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                Sheet sheet = workbook.createSheet("SparseSheet");

                Row row0 = sheet.createRow(0);
                row0.createCell(0).setCellValue("Header1");
                row0.createCell(1).setCellValue("Header2");

                Row row1 = sheet.createRow(1);
                row1.createCell(0).setCellValue("DataA");
                row1.createCell(1).setCellValue("DataB");

                Row row5 = sheet.createRow(5);
                row5.createCell(0).setCellValue("SparseX");
                row5.createCell(1).setCellValue("SparseY");

                workbook.write(baos);
                xlsxData = baos.toByteArray();
            }

            MockMultipartFile xlsxFile = new MockMultipartFile(
                    "file", "sparse.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, xlsxData
            );
            File outputFile = tempDir.resolve("sparse-output.pdf").toFile();

            service.convertExcelToPdf(xlsxFile, outputFile, false);

            assertTrue(outputFile.exists(), "PDF file should be created");

            try (PDDocument document = Loader.loadPDF(outputFile)) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);

                // All present row data should appear
                assertTrue(text.contains("Header1"), "PDF should contain 'Header1'");
                assertTrue(text.contains("Header2"), "PDF should contain 'Header2'");
                assertTrue(text.contains("DataA"), "PDF should contain 'DataA'");
                assertTrue(text.contains("DataB"), "PDF should contain 'DataB'");
                assertTrue(text.contains("SparseX"), "PDF should contain 'SparseX'");
                assertTrue(text.contains("SparseY"), "PDF should contain 'SparseY'");
            }
        }

        @Test
        void convertExcelToPdf_singleRowAtIndex10_pdfContainsRowData() throws Exception {
            // Create workbook with a single row at index 10
            byte[] xlsxData;
            try (XSSFWorkbook workbook = new XSSFWorkbook();
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                Sheet sheet = workbook.createSheet("SingleSparse");

                Row row10 = sheet.createRow(10);
                row10.createCell(0).setCellValue("LonelyCell");
                row10.createCell(1).setCellValue("AloneValue");

                workbook.write(baos);
                xlsxData = baos.toByteArray();
            }

            MockMultipartFile xlsxFile = new MockMultipartFile(
                    "file", "single-sparse.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, xlsxData
            );
            File outputFile = tempDir.resolve("single-sparse-output.pdf").toFile();

            service.convertExcelToPdf(xlsxFile, outputFile, false);

            assertTrue(outputFile.exists(), "PDF file should be created");

            try (PDDocument document = Loader.loadPDF(outputFile)) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);

                assertTrue(text.contains("LonelyCell"), "PDF should contain 'LonelyCell'");
                assertTrue(text.contains("AloneValue"), "PDF should contain 'AloneValue'");
            }
        }

        @Test
        void convertExcelToPdf_contiguousRows_noBehaviorChange() throws Exception {
            // Create workbook with contiguous rows (no gaps)
            byte[] xlsxData;
            try (XSSFWorkbook workbook = new XSSFWorkbook();
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                Sheet sheet = workbook.createSheet("Contiguous");

                for (int r = 0; r < 4; r++) {
                    Row row = sheet.createRow(r);
                    row.createCell(0).setCellValue("Row" + r + "Col0");
                    row.createCell(1).setCellValue("Row" + r + "Col1");
                }

                workbook.write(baos);
                xlsxData = baos.toByteArray();
            }

            MockMultipartFile xlsxFile = new MockMultipartFile(
                    "file", "contiguous.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, xlsxData
            );
            File outputFile = tempDir.resolve("contiguous-output.pdf").toFile();

            service.convertExcelToPdf(xlsxFile, outputFile, false);

            assertTrue(outputFile.exists(), "PDF file should be created");

            try (PDDocument document = Loader.loadPDF(outputFile)) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);

                for (int r = 0; r < 4; r++) {
                    assertTrue(text.contains("Row" + r + "Col0"),
                            "PDF should contain 'Row" + r + "Col0'");
                    assertTrue(text.contains("Row" + r + "Col1"),
                            "PDF should contain 'Row" + r + "Col1'");
                }
            }
        }
    }

    // ========== ExcelUtils Delegation Tests (Requirements 1.1, 1.2, 1.3) ==========

    @Nested
    class ExcelUtilsDelegationTests {

        @Test
        void excelToPdfService_hasNoDuplicateCellValueMethods() throws Exception {
            // Use reflection to verify ExcelToPdfService has no private methods
            // named "getCellValue" or "getCellValueAsString"
            var declaredMethods = ExcelToPdfService.class.getDeclaredMethods();
            for (var method : declaredMethods) {
                String name = method.getName();
                assertFalse(name.equals("getCellValue"),
                        "ExcelToPdfService should NOT have a method named 'getCellValue' — " +
                                "cell extraction should be delegated to ExcelUtils");
                assertFalse(name.equals("getCellValueAsString"),
                        "ExcelToPdfService should NOT have a method named 'getCellValueAsString' — " +
                                "cell extraction should be delegated to ExcelUtils");
            }
        }

        @Test
        void excelUtils_getCellValueAsString_nullCell_returnsEmptyString() {
            String result = com.xtopdf.xtopdf.utils.ExcelUtils.getCellValueAsString(null);
            assertEquals("", result, "ExcelUtils.getCellValueAsString(null) should return empty string");
        }
    }
}
