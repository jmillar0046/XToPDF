package com.xtopdf.xtopdf.services.conversion.spreadsheet;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Font;
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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;

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

    // ========== ExecuteMacros Formula Recalculation Tests (Requirements 2.1, 2.2, 2.3) ==========

    @Nested
    class ExecuteMacrosTests {

        private ExcelToPdfService service;
        private PdfBackendProvider pdfBackend;

        @BeforeEach
        void setUp() {
            pdfBackend = new PdfBoxBackend();
            service = new ExcelToPdfService(pdfBackend);
        }

        @Test
        void convertExcelToPdf_executeMacrosTrue_formulasAreRecalculated() throws Exception {
            // Create workbook with numeric values and a SUM formula
            byte[] xlsxData;
            try (XSSFWorkbook workbook = new XSSFWorkbook();
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                Sheet sheet = workbook.createSheet("FormulaSheet");

                Row dataRow = sheet.createRow(0);
                dataRow.createCell(0).setCellValue(10);
                dataRow.createCell(1).setCellValue(20);
                dataRow.createCell(2).setCellValue(30);

                Row formulaRow = sheet.createRow(1);
                Cell sumCell = formulaRow.createCell(0);
                sumCell.setCellFormula("SUM(A1:C1)");
                // Do NOT evaluate — leave cached result empty/zero

                workbook.write(baos);
                xlsxData = baos.toByteArray();
            }

            MockMultipartFile xlsxFile = new MockMultipartFile(
                    "file", "formulas.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, xlsxData
            );
            File outputFile = tempDir.resolve("formulas-output.pdf").toFile();

            // Convert with executeMacros=true — should recalculate formulas
            service.convertExcelToPdf(xlsxFile, outputFile, true);

            assertTrue(outputFile.exists(), "PDF file should be created");

            try (PDDocument document = Loader.loadPDF(outputFile)) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);

                // The SUM formula should have been recalculated to 60
                assertTrue(text.contains("60"),
                        "PDF should contain recalculated SUM result '60' but text was: " + text);
            }
        }

        @Test
        void convertExcelToPdf_executeMacrosFalse_cachedValuesUsed() throws Exception {
            // Create workbook with a formula — conversion should still work with cached values
            byte[] xlsxData;
            try (XSSFWorkbook workbook = new XSSFWorkbook();
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                Sheet sheet = workbook.createSheet("CachedSheet");

                Row dataRow = sheet.createRow(0);
                dataRow.createCell(0).setCellValue(5);
                dataRow.createCell(1).setCellValue(15);

                Row formulaRow = sheet.createRow(1);
                Cell sumCell = formulaRow.createCell(0);
                sumCell.setCellFormula("SUM(A1:B1)");
                // Pre-evaluate to cache the result
                FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                evaluator.evaluateAll();

                workbook.write(baos);
                xlsxData = baos.toByteArray();
            }

            MockMultipartFile xlsxFile = new MockMultipartFile(
                    "file", "cached.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, xlsxData
            );
            File outputFile = tempDir.resolve("cached-output.pdf").toFile();

            // Convert with executeMacros=false — should use cached values
            service.convertExcelToPdf(xlsxFile, outputFile, false);

            assertTrue(outputFile.exists(), "PDF file should be created");
            assertTrue(outputFile.length() > 0, "PDF file should not be empty");

            try (PDDocument document = Loader.loadPDF(outputFile)) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);

                // The cached SUM result (20) should appear in the PDF
                assertTrue(text.contains("20"),
                        "PDF should contain cached SUM result '20' but text was: " + text);
            }
        }

        @Test
        void convertExcelToPdf_formulaRecalculationFails_conversionContinues() throws Exception {
            // Create workbook with an invalid formula reference that will fail during recalculation
            byte[] xlsxData;
            try (XSSFWorkbook workbook = new XSSFWorkbook();
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                Sheet sheet = workbook.createSheet("BadFormulaSheet");

                Row dataRow = sheet.createRow(0);
                dataRow.createCell(0).setCellValue("ValidData");

                Row formulaRow = sheet.createRow(1);
                Cell badCell = formulaRow.createCell(0);
                // Reference a non-existent sheet — this will fail during recalculation
                badCell.setCellFormula("NonExistentSheet!A1");

                workbook.write(baos);
                xlsxData = baos.toByteArray();
            }

            MockMultipartFile xlsxFile = new MockMultipartFile(
                    "file", "bad-formula.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, xlsxData
            );
            File outputFile = tempDir.resolve("bad-formula-output.pdf").toFile();

            // Should NOT throw — conversion should continue with cached/fallback values
            assertDoesNotThrow(() ->
                    service.convertExcelToPdf(xlsxFile, outputFile, true),
                    "Conversion should not throw when formula recalculation fails"
            );

            assertTrue(outputFile.exists(), "PDF file should be created even when formula recalculation fails");
            assertTrue(outputFile.length() > 0, "PDF file should not be empty");

            try (PDDocument document = Loader.loadPDF(outputFile)) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);

                // The valid data should still be present
                assertTrue(text.contains("ValidData"),
                        "PDF should contain 'ValidData' even when formula recalculation fails, but text was: " + text);
            }
        }
    }

    // ========== Page Break Tests (Requirements 6.1, 6.2, 6.3) ==========

    @Nested
    class PageBreakTests {

        private ExcelToPdfService service;
        private PdfBackendProvider pdfBackend;

        @BeforeEach
        void setUp() {
            pdfBackend = new PdfBoxBackend();
            service = new ExcelToPdfService(pdfBackend);
        }

        @Test
        void convertExcelToPdf_threeSheetWorkbook_producesAtLeastThreePages() throws Exception {
            byte[] xlsxData;
            try (XSSFWorkbook workbook = new XSSFWorkbook();
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                for (int s = 0; s < 3; s++) {
                    Sheet sheet = workbook.createSheet("Sheet" + (s + 1));
                    Row row = sheet.createRow(0);
                    row.createCell(0).setCellValue("Data on sheet " + (s + 1));
                }
                workbook.write(baos);
                xlsxData = baos.toByteArray();
            }

            MockMultipartFile xlsxFile = new MockMultipartFile(
                    "file", "multi-sheet.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, xlsxData
            );
            File outputFile = tempDir.resolve("multi-sheet-output.pdf").toFile();

            service.convertExcelToPdf(xlsxFile, outputFile, false);

            assertTrue(outputFile.exists(), "PDF file should be created");

            try (PDDocument document = Loader.loadPDF(outputFile)) {
                assertTrue(document.getNumberOfPages() >= 3,
                        "3-sheet workbook should produce at least 3 PDF pages, but got: " + document.getNumberOfPages());
            }
        }

        @Test
        void convertExcelToPdf_singleSheetWorkbook_producesOnePage() throws Exception {
            byte[] xlsxData;
            try (XSSFWorkbook workbook = new XSSFWorkbook();
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                Sheet sheet = workbook.createSheet("OnlySheet");
                Row row = sheet.createRow(0);
                row.createCell(0).setCellValue("Single sheet data");
                workbook.write(baos);
                xlsxData = baos.toByteArray();
            }

            MockMultipartFile xlsxFile = new MockMultipartFile(
                    "file", "single-sheet.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, xlsxData
            );
            File outputFile = tempDir.resolve("single-sheet-output.pdf").toFile();

            service.convertExcelToPdf(xlsxFile, outputFile, false);

            assertTrue(outputFile.exists(), "PDF file should be created");

            try (PDDocument document = Loader.loadPDF(outputFile)) {
                assertEquals(1, document.getNumberOfPages(),
                        "Single-sheet workbook should produce exactly 1 PDF page");
            }
        }

        @Test
        void convertExcelToPdf_multiSheetWorkbook_sheetNamesAppearInPdf() throws Exception {
            byte[] xlsxData;
            try (XSSFWorkbook workbook = new XSSFWorkbook();
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                String[] sheetNames = {"Revenue", "Expenses", "Summary"};
                for (String name : sheetNames) {
                    Sheet sheet = workbook.createSheet(name);
                    Row row = sheet.createRow(0);
                    row.createCell(0).setCellValue("Data for " + name);
                }
                workbook.write(baos);
                xlsxData = baos.toByteArray();
            }

            MockMultipartFile xlsxFile = new MockMultipartFile(
                    "file", "named-sheets.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, xlsxData
            );
            File outputFile = tempDir.resolve("named-sheets-output.pdf").toFile();

            service.convertExcelToPdf(xlsxFile, outputFile, false);

            assertTrue(outputFile.exists(), "PDF file should be created");

            try (PDDocument document = Loader.loadPDF(outputFile)) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);

                assertTrue(text.contains("Revenue"),
                        "PDF should contain sheet name 'Revenue' but text was: " + text);
                assertTrue(text.contains("Expenses"),
                        "PDF should contain sheet name 'Expenses' but text was: " + text);
                assertTrue(text.contains("Summary"),
                        "PDF should contain sheet name 'Summary' but text was: " + text);
            }
        }
    }

    // ========== Integration Tests (Requirements 10.1, 10.2, 10.3, 10.4, 10.5, 10.6) ==========

    @Nested
    class IntegrationTests {

        private ExcelToPdfService service;
        private PdfBackendProvider pdfBackend;

        @BeforeEach
        void setUp() {
            pdfBackend = new PdfBoxBackend();
            service = new ExcelToPdfService(pdfBackend);
        }

        @Test
        void convertExcelToPdf_basicSpreadsheetXlsx_producesValidPdfWithExpectedContent() throws Exception {
            ClassPathResource resource = new ClassPathResource("test-files/basic-spreadsheet.xlsx");
            byte[] xlsxData = Files.readAllBytes(resource.getFile().toPath());
            MockMultipartFile xlsxFile = new MockMultipartFile(
                "file", "basic-spreadsheet.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, xlsxData
            );
            File outputFile = tempDir.resolve("integration-xlsx-output.pdf").toFile();

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

                // Verify expected data from basic-spreadsheet.xlsx
                assertTrue(text.contains("Basic Data"), "PDF should contain sheet name 'Basic Data'");
                assertTrue(text.contains("Name"), "PDF should contain header 'Name'");
                assertTrue(text.contains("Age"), "PDF should contain header 'Age'");
                assertTrue(text.contains("City"), "PDF should contain header 'City'");
                assertTrue(text.contains("Salary"), "PDF should contain header 'Salary'");
            }
        }

        @Test
        void convertExcelToPdf_xlsFile_producesValidPdfWithExpectedContent() throws Exception {
            // Create a programmatic .xls file to test XLS format through the unified service
            byte[] xlsData;
            try (HSSFWorkbook workbook = new HSSFWorkbook();
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                Sheet sheet = workbook.createSheet("XLS Integration");
                Row header = sheet.createRow(0);
                header.createCell(0).setCellValue("Product");
                header.createCell(1).setCellValue("Price");
                header.createCell(2).setCellValue("Quantity");

                Row row1 = sheet.createRow(1);
                row1.createCell(0).setCellValue("Widget");
                row1.createCell(1).setCellValue(9.99);
                row1.createCell(2).setCellValue(100);

                Row row2 = sheet.createRow(2);
                row2.createCell(0).setCellValue("Gadget");
                row2.createCell(1).setCellValue(24.50);
                row2.createCell(2).setCellValue(50);

                workbook.write(baos);
                xlsData = baos.toByteArray();
            }

            MockMultipartFile xlsFile = new MockMultipartFile(
                "file", "integration-test.xls", MediaType.APPLICATION_OCTET_STREAM_VALUE, xlsData
            );
            File outputFile = tempDir.resolve("integration-xls-output.pdf").toFile();

            service.convertExcelToPdf(xlsFile, outputFile, false);

            assertTrue(outputFile.exists(), "PDF file should be created");
            assertTrue(outputFile.length() > 0, "PDF file should not be empty");

            try (PDDocument document = Loader.loadPDF(outputFile)) {
                assertNotNull(document);
                assertTrue(document.getNumberOfPages() > 0, "PDF should have at least one page");

                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);
                assertNotNull(text);

                // Verify XLS data appears in PDF
                assertTrue(text.contains("Product"), "PDF should contain 'Product'");
                assertTrue(text.contains("Widget"), "PDF should contain 'Widget'");
                assertTrue(text.contains("Gadget"), "PDF should contain 'Gadget'");
                assertTrue(text.contains("XLS Integration"), "PDF should contain sheet name 'XLS Integration'");
            }
        }

        @Test
        void convertExcelToPdf_multiSheetWithFormattingSparseRowsAndFormulas_endToEnd() throws Exception {
            // Create a complex workbook with multiple sheets, formatting, sparse rows, and formulas
            byte[] xlsxData;
            try (XSSFWorkbook workbook = new XSSFWorkbook();
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

                // Sheet 1: Data with formatting
                Sheet dataSheet = workbook.createSheet("DataSheet");
                org.apache.poi.ss.usermodel.CellStyle boldStyle = workbook.createCellStyle();
                Font boldFont = workbook.createFont();
                boldFont.setBold(true);
                boldStyle.setFont(boldFont);

                Row headerRow = dataSheet.createRow(0);
                Cell h1 = headerRow.createCell(0);
                h1.setCellValue("Item");
                h1.setCellStyle(boldStyle);
                Cell h2 = headerRow.createCell(1);
                h2.setCellValue("Value");
                h2.setCellStyle(boldStyle);

                Row dataRow1 = dataSheet.createRow(1);
                dataRow1.createCell(0).setCellValue("Alpha");
                dataRow1.createCell(1).setCellValue(100);

                Row dataRow2 = dataSheet.createRow(2);
                dataRow2.createCell(0).setCellValue("Beta");
                dataRow2.createCell(1).setCellValue(200);

                // Sheet 2: Sparse rows
                Sheet sparseSheet = workbook.createSheet("SparseSheet");
                Row sr0 = sparseSheet.createRow(0);
                sr0.createCell(0).setCellValue("First");
                // Gap at rows 1-4
                Row sr5 = sparseSheet.createRow(5);
                sr5.createCell(0).setCellValue("Sixth");
                Row sr10 = sparseSheet.createRow(10);
                sr10.createCell(0).setCellValue("Eleventh");

                // Sheet 3: Formulas
                Sheet formulaSheet = workbook.createSheet("FormulaSheet");
                Row fr0 = formulaSheet.createRow(0);
                fr0.createCell(0).setCellValue(10);
                fr0.createCell(1).setCellValue(20);
                fr0.createCell(2).setCellValue(30);
                Row fr1 = formulaSheet.createRow(1);
                Cell sumCell = fr1.createCell(0);
                sumCell.setCellFormula("SUM(A1:C1)");
                // Pre-evaluate
                org.apache.poi.ss.usermodel.FormulaEvaluator evaluator =
                    workbook.getCreationHelper().createFormulaEvaluator();
                evaluator.evaluateAll();

                workbook.write(baos);
                xlsxData = baos.toByteArray();
            }

            MockMultipartFile xlsxFile = new MockMultipartFile(
                "file", "complex-integration.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, xlsxData
            );
            File outputFile = tempDir.resolve("complex-integration-output.pdf").toFile();

            service.convertExcelToPdf(xlsxFile, outputFile, false);

            assertTrue(outputFile.exists(), "PDF file should be created");
            assertTrue(outputFile.length() > 0, "PDF file should not be empty");

            try (PDDocument document = Loader.loadPDF(outputFile)) {
                // Should have at least 3 pages (one per sheet)
                assertTrue(document.getNumberOfPages() >= 3,
                    "3-sheet workbook should produce at least 3 PDF pages, got: " + document.getNumberOfPages());

                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);

                // Verify all sheet names
                assertTrue(text.contains("DataSheet"), "PDF should contain sheet name 'DataSheet'");
                assertTrue(text.contains("SparseSheet"), "PDF should contain sheet name 'SparseSheet'");
                assertTrue(text.contains("FormulaSheet"), "PDF should contain sheet name 'FormulaSheet'");

                // Verify data from Sheet 1
                assertTrue(text.contains("Item"), "PDF should contain 'Item'");
                assertTrue(text.contains("Alpha"), "PDF should contain 'Alpha'");
                assertTrue(text.contains("Beta"), "PDF should contain 'Beta'");

                // Verify sparse row data from Sheet 2
                assertTrue(text.contains("First"), "PDF should contain 'First' from sparse sheet");
                assertTrue(text.contains("Sixth"), "PDF should contain 'Sixth' from sparse sheet");
                assertTrue(text.contains("Eleventh"), "PDF should contain 'Eleventh' from sparse sheet");

                // Verify formula result from Sheet 3 (SUM of 10+20+30 = 60)
                assertTrue(text.contains("60"),
                    "PDF should contain formula result '60' but text was: " + text);
            }
        }

        @Test
        void convertExcelToPdf_backwardCompatibility_xlsxOutputContainsExpectedData() throws Exception {
            // Verify that the unified service produces the same output as the old XlsxToPdfService
            ClassPathResource resource = new ClassPathResource("test-files/basic-spreadsheet.xlsx");
            byte[] xlsxData = Files.readAllBytes(resource.getFile().toPath());
            MockMultipartFile xlsxFile = new MockMultipartFile(
                "file", "basic-spreadsheet.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, xlsxData
            );
            File outputFile = tempDir.resolve("backward-compat-xlsx.pdf").toFile();

            service.convertExcelToPdf(xlsxFile, outputFile, false);

            assertTrue(outputFile.exists(), "PDF file should be created");

            try (PDDocument document = Loader.loadPDF(outputFile)) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);

                // Same assertions as the old XlsxToPdfServiceTest
                assertTrue(text.contains("Basic Data"), "PDF should contain sheet name 'Basic Data'");
                assertTrue(text.contains("Name"), "PDF should contain header 'Name'");
                assertTrue(text.contains("Age"), "PDF should contain header 'Age'");
                assertTrue(text.contains("City"), "PDF should contain header 'City'");
                assertTrue(text.contains("Salary"), "PDF should contain header 'Salary'");
                assertTrue(text.contains("Alice") || text.contains("Bob") || text.contains("Charlie"),
                    "PDF should contain employee names");
            }
        }

        @Test
        void convertExcelToPdf_backwardCompatibility_xlsOutputContainsExpectedData() throws Exception {
            // Verify that the unified service produces the same output as the old XlsToPdfService
            ClassPathResource resource = new ClassPathResource("test-files/basic-spreadsheet.xls");
            byte[] xlsData = Files.readAllBytes(resource.getFile().toPath());
            MockMultipartFile xlsFile = new MockMultipartFile(
                "file", "basic-spreadsheet.xls", MediaType.APPLICATION_OCTET_STREAM_VALUE, xlsData
            );
            File outputFile = tempDir.resolve("backward-compat-xls.pdf").toFile();

            service.convertExcelToPdf(xlsFile, outputFile, false);

            assertTrue(outputFile.exists(), "PDF file should be created");

            try (PDDocument document = Loader.loadPDF(outputFile)) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);

                // Same assertions as the old XlsToPdfServiceTest
                assertTrue(text.contains("Basic Data"), "PDF should contain sheet name 'Basic Data'");
                assertTrue(text.contains("Name"), "PDF should contain header 'Name'");
                assertTrue(text.contains("Age"), "PDF should contain header 'Age'");
                assertTrue(text.contains("City"), "PDF should contain header 'City'");
                assertTrue(text.contains("Salary"), "PDF should contain header 'Salary'");
            }
        }
    }

    // ========== Streaming Threshold Routing Tests (Requirements 9.1, 9.2) ==========

    @Nested
    class StreamingThresholdRoutingTests {

        private ExcelToPdfService service;

        @BeforeEach
        void setUp() {
            PdfBackendProvider mockBackend = mock(PdfBackendProvider.class);
            service = new ExcelToPdfService(mockBackend);
        }

        @Test
        void isStreamingRequired_xlsxFileAboveThreshold_returnsTrue() {
            MockMultipartFile largeXlsx = new MockMultipartFile(
                "file", "large.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[]{1}
            ) {
                @Override
                public long getSize() {
                    return ExcelToPdfService.STREAMING_THRESHOLD + 1;
                }
            };

            assertTrue(service.isStreamingRequired(largeXlsx),
                "XLSX file above 10MB threshold should require streaming");
        }

        @Test
        void isStreamingRequired_xlsxFileAtOrBelowThreshold_returnsFalse() {
            MockMultipartFile smallXlsx = new MockMultipartFile(
                "file", "small.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[]{1}
            ) {
                @Override
                public long getSize() {
                    return ExcelToPdfService.STREAMING_THRESHOLD;
                }
            };

            assertFalse(service.isStreamingRequired(smallXlsx),
                "XLSX file at or below 10MB threshold should NOT require streaming");
        }

        @Test
        void isStreamingRequired_xlsFileAboveThreshold_returnsFalse() {
            MockMultipartFile largeXls = new MockMultipartFile(
                "file", "large.xls", MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[]{1}
            ) {
                @Override
                public long getSize() {
                    return ExcelToPdfService.STREAMING_THRESHOLD + 1;
                }
            };

            assertFalse(service.isStreamingRequired(largeXls),
                "XLS file above threshold should NOT require streaming (no SAX for binary XLS)");
        }

        @Test
        void isStreamingRequired_xlsxFileWellBelowThreshold_returnsFalse() {
            MockMultipartFile tinyXlsx = new MockMultipartFile(
                "file", "tiny.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[]{1}
            ) {
                @Override
                public long getSize() {
                    return 1024L; // 1 KB
                }
            };

            assertFalse(service.isStreamingRequired(tinyXlsx),
                "Small XLSX file should NOT require streaming");
        }

        @Test
        void isStreamingRequired_nullFilename_returnsFalse() {
            MockMultipartFile noName = new MockMultipartFile(
                "file", null, MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[]{1}
            ) {
                @Override
                public long getSize() {
                    return ExcelToPdfService.STREAMING_THRESHOLD + 1;
                }
            };

            assertFalse(service.isStreamingRequired(noName),
                "File with null filename should NOT require streaming");
        }
    }

    // ========== Streaming Chunked Processing Tests (Requirements 9.3, 9.4) ==========

    @Nested
    class StreamingChunkedProcessingTests {

        private ExcelToPdfService service;
        private PdfBackendProvider pdfBackend;

        @BeforeEach
        void setUp() {
            pdfBackend = new PdfBoxBackend();
            service = new ExcelToPdfService(pdfBackend);
        }

        @Test
        void convertXlsxStreaming_validXlsx_producesValidPdfWithCorrectContent() throws Exception {
            // Create a valid XLSX workbook programmatically
            byte[] xlsxData;
            try (XSSFWorkbook workbook = new XSSFWorkbook();
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                Sheet sheet = workbook.createSheet("StreamSheet");
                Row row0 = sheet.createRow(0);
                row0.createCell(0).setCellValue("StreamHeader1");
                row0.createCell(1).setCellValue("StreamHeader2");
                Row row1 = sheet.createRow(1);
                row1.createCell(0).setCellValue("StreamData1");
                row1.createCell(1).setCellValue("StreamData2");
                workbook.write(baos);
                xlsxData = baos.toByteArray();
            }

            MockMultipartFile xlsxFile = new MockMultipartFile(
                "file", "stream-test.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, xlsxData
            );
            File outputFile = tempDir.resolve("stream-output.pdf").toFile();

            // Call streaming path directly
            service.convertXlsxStreaming(xlsxFile, outputFile);

            assertTrue(outputFile.exists(), "PDF file should be created");
            assertTrue(outputFile.length() > 0, "PDF file should not be empty");

            try (PDDocument document = Loader.loadPDF(outputFile)) {
                assertNotNull(document);
                assertTrue(document.getNumberOfPages() > 0, "PDF should have at least one page");

                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);

                assertTrue(text.contains("StreamHeader1"),
                    "PDF should contain 'StreamHeader1' but text was: " + text);
                assertTrue(text.contains("StreamHeader2"),
                    "PDF should contain 'StreamHeader2' but text was: " + text);
                assertTrue(text.contains("StreamData1"),
                    "PDF should contain 'StreamData1' but text was: " + text);
                assertTrue(text.contains("StreamData2"),
                    "PDF should contain 'StreamData2' but text was: " + text);
            }
        }

        @Test
        void convertXlsxStreaming_multiSheet_maintainsSheetNames() throws Exception {
            byte[] xlsxData;
            try (XSSFWorkbook workbook = new XSSFWorkbook();
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                String[] sheetNames = {"Sales", "Inventory", "Reports"};
                for (String name : sheetNames) {
                    Sheet sheet = workbook.createSheet(name);
                    Row row = sheet.createRow(0);
                    row.createCell(0).setCellValue("Data_" + name);
                }
                workbook.write(baos);
                xlsxData = baos.toByteArray();
            }

            MockMultipartFile xlsxFile = new MockMultipartFile(
                "file", "multi-stream.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, xlsxData
            );
            File outputFile = tempDir.resolve("multi-stream-output.pdf").toFile();

            service.convertXlsxStreaming(xlsxFile, outputFile);

            assertTrue(outputFile.exists(), "PDF file should be created");

            try (PDDocument document = Loader.loadPDF(outputFile)) {
                assertTrue(document.getNumberOfPages() >= 3,
                    "3-sheet workbook should produce at least 3 PDF pages via streaming, but got: "
                        + document.getNumberOfPages());

                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);

                assertTrue(text.contains("Sales"),
                    "PDF should contain sheet name 'Sales' but text was: " + text);
                assertTrue(text.contains("Inventory"),
                    "PDF should contain sheet name 'Inventory' but text was: " + text);
                assertTrue(text.contains("Reports"),
                    "PDF should contain sheet name 'Reports' but text was: " + text);
                assertTrue(text.contains("Data_Sales"),
                    "PDF should contain 'Data_Sales' but text was: " + text);
                assertTrue(text.contains("Data_Inventory"),
                    "PDF should contain 'Data_Inventory' but text was: " + text);
                assertTrue(text.contains("Data_Reports"),
                    "PDF should contain 'Data_Reports' but text was: " + text);
            }
        }
    }
}
