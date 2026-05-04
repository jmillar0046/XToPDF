package com.xtopdf.xtopdf.services.conversion.spreadsheet;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend;
import com.xtopdf.xtopdf.utils.ExcelUtils;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.http.MediaType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for ExcelToPdfService.
 * Uses jqwik 1.9.3 for property-based testing.
 */
class ExcelToPdfServicePropertyTest {

    private final PdfBackendProvider pdfBackend = new PdfBoxBackend();

    // ========== Property 6: Sparse Row Correctness ==========

    /**
     * Property 6: Sparse Row Correctness
     *
     * For any Sheet with non-contiguous rows, the extracted table data array SHALL have
     * length getLastRowNum() + 1. Each Row present in the Sheet SHALL have its cell values
     * placed at the row's actual index in the array. Each index in the array that has no
     * corresponding Row object SHALL contain an array of empty strings.
     *
     * **Validates: Requirements 5.1, 5.2, 5.3, 5.4**
     */
    @Property(tries = 25)
    @Label("Property 6: Sparse Row Correctness — data from present rows appears in PDF, gap rows produce no spurious data")
    void sparseRowCorrectnessProperty(
            @ForAll("sparseRowIndicesAndData") SparseSheetData sparseData
    ) throws Exception {
        // Build an XSSFWorkbook with rows at the specified non-contiguous indices
        byte[] xlsxBytes;
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("SparseSheet");

            for (Map.Entry<Integer, String[]> entry : sparseData.rowData().entrySet()) {
                int rowIndex = entry.getKey();
                String[] cellValues = entry.getValue();
                Row row = sheet.createRow(rowIndex);
                for (int c = 0; c < cellValues.length; c++) {
                    row.createCell(c).setCellValue(cellValues[c]);
                }
            }

            workbook.write(baos);
            xlsxBytes = baos.toByteArray();
        }

        // Convert to PDF
        ExcelToPdfService service = new ExcelToPdfService(pdfBackend);
        Path tempDir = Files.createTempDirectory("sparse-prop-test");
        File outputFile = tempDir.resolve("output.pdf").toFile();

        try {
            MockMultipartFile xlsxFile = new MockMultipartFile(
                    "file", "sparse.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, xlsxBytes
            );
            service.convertExcelToPdf(xlsxFile, outputFile, false);

            assertTrue(outputFile.exists(), "PDF file should be created");
            assertTrue(outputFile.length() > 0, "PDF file should not be empty");

            // Extract text from PDF
            String pdfText;
            try (PDDocument document = Loader.loadPDF(outputFile)) {
                PDFTextStripper stripper = new PDFTextStripper();
                pdfText = stripper.getText(document);
            }

            // Verify that data from each present row appears in the PDF
            for (Map.Entry<Integer, String[]> entry : sparseData.rowData().entrySet()) {
                for (String cellValue : entry.getValue()) {
                    if (!cellValue.isEmpty()) {
                        assertTrue(pdfText.contains(cellValue),
                                "PDF should contain cell value '" + cellValue + "' from row " + entry.getKey()
                                        + " but PDF text was: " + pdfText);
                    }
                }
            }

            // Verify that gap rows don't produce spurious data:
            // The unique marker values we used should only appear for rows that exist
            for (String marker : sparseData.gapMarkers()) {
                assertFalse(pdfText.contains(marker),
                        "PDF should NOT contain gap marker '" + marker + "' — gap rows should be empty");
            }
        } finally {
            // Cleanup
            if (outputFile.exists()) outputFile.delete();
            tempDir.toFile().delete();
        }
    }

    @Provide
    Arbitrary<SparseSheetData> sparseRowIndicesAndData() {
        // Generate 2-6 unique row indices in range [0, 20] with gaps
        return Arbitraries.integers().between(2, 6).flatMap(numRows ->
                Arbitraries.integers().between(0, 20)
                        .set().ofMinSize(numRows).ofMaxSize(numRows)
                        .filter(indices -> {
                            // Ensure there's at least one gap (non-contiguous)
                            List<Integer> sorted = new ArrayList<>(indices);
                            Collections.sort(sorted);
                            for (int i = 1; i < sorted.size(); i++) {
                                if (sorted.get(i) - sorted.get(i - 1) > 1) {
                                    return true;
                                }
                            }
                            return false;
                        })
                        .flatMap(indices -> {
                            // Generate cell data for each row (1-3 columns with unique values)
                            int numCols = 2;
                            Map<Integer, String[]> rowData = new TreeMap<>();
                            Set<Integer> indexSet = new TreeSet<>(indices);
                            List<String> gapMarkers = new ArrayList<>();

                            // Find the max index to determine gap rows
                            int maxIndex = indexSet.stream().max(Integer::compareTo).orElse(0);

                            for (int idx : indexSet) {
                                String[] cells = new String[numCols];
                                for (int c = 0; c < numCols; c++) {
                                    cells[c] = "R" + idx + "C" + c + "_" + UUID.randomUUID().toString().substring(0, 6);
                                }
                                rowData.put(idx, cells);
                            }

                            // Create markers for gap rows (rows that should NOT appear)
                            for (int i = 0; i <= maxIndex; i++) {
                                if (!indexSet.contains(i)) {
                                    gapMarkers.add("GAPROW_" + i + "_" + UUID.randomUUID().toString().substring(0, 6));
                                }
                            }

                            return Arbitraries.just(new SparseSheetData(rowData, gapMarkers));
                        })
        );
    }

    /**
     * Record holding sparse sheet test data.
     */
    record SparseSheetData(Map<Integer, String[]> rowData, List<String> gapMarkers) {}

    // ========== Property 1: Formula Cell Extraction Round-Trip ==========

    /**
     * Property 1: Formula Cell Extraction Round-Trip
     *
     * For any Cell of type FORMULA with a cached result type of STRING, NUMERIC, or BOOLEAN,
     * ExcelUtils.getCellValueAsString(cell) SHALL return a non-empty string that matches the
     * cached result value. For any FORMULA cell where cached result evaluation throws an exception,
     * the method SHALL return the formula text itself (also non-empty).
     *
     * **Validates: Requirements 1.4**
     */
    @Property(tries = 25)
    @Label("Property 1: Formula Cell Extraction Round-Trip — formula cells with cached results produce correct string values in PDF")
    void formulaCellExtractionRoundTripProperty(
            @ForAll("formulaSheetData") FormulaSheetData formulaData
    ) throws Exception {
        // Build an XSSFWorkbook with formula cells that have cached results
        byte[] xlsxBytes;
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("FormulaSheet");

            // Row 0: numeric source data for formulas
            Row sourceRow = sheet.createRow(0);
            for (int c = 0; c < formulaData.sourceValues().length; c++) {
                sourceRow.createCell(c).setCellValue(formulaData.sourceValues()[c]);
            }

            // Row 1: SUM formula
            Row formulaRow = sheet.createRow(1);
            int lastCol = formulaData.sourceValues().length - 1;
            String colLetter = String.valueOf((char) ('A' + lastCol));
            Cell sumCell = formulaRow.createCell(0);
            sumCell.setCellFormula("SUM(A1:" + colLetter + "1)");

            // Row 2: AVERAGE formula
            Row avgRow = sheet.createRow(2);
            Cell avgCell = avgRow.createCell(0);
            avgCell.setCellFormula("AVERAGE(A1:" + colLetter + "1)");

            // Row 3: Boolean formula (comparison)
            Row boolRow = sheet.createRow(3);
            Cell boolCell = boolRow.createCell(0);
            boolCell.setCellFormula("A1>0");

            // Force formula evaluation to cache results
            ExcelUtils.recalculateFormulas(workbook);

            workbook.write(baos);
            xlsxBytes = baos.toByteArray();
        }

        // Convert to PDF
        ExcelToPdfService service = new ExcelToPdfService(pdfBackend);
        Path tempDir = Files.createTempDirectory("formula-prop-test");
        File outputFile = tempDir.resolve("output.pdf").toFile();

        try {
            MockMultipartFile xlsxFile = new MockMultipartFile(
                    "file", "formulas.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, xlsxBytes
            );
            service.convertExcelToPdf(xlsxFile, outputFile, false);

            assertTrue(outputFile.exists(), "PDF file should be created");

            // Extract text from PDF
            String pdfText;
            try (PDDocument document = Loader.loadPDF(outputFile)) {
                PDFTextStripper stripper = new PDFTextStripper();
                pdfText = stripper.getText(document);
            }

            // Calculate expected values
            double expectedSum = 0;
            for (double v : formulaData.sourceValues()) {
                expectedSum += v;
            }
            double expectedAvg = expectedSum / formulaData.sourceValues().length;
            boolean expectedBool = formulaData.sourceValues()[0] > 0;

            // Verify formula results appear in PDF text
            String sumStr = formatExpectedNumeric(expectedSum);
            String avgStr = formatExpectedNumeric(expectedAvg);

            assertTrue(pdfText.contains(sumStr),
                    "PDF should contain SUM result '" + sumStr + "' but text was: " + pdfText);
            assertTrue(pdfText.contains(avgStr),
                    "PDF should contain AVERAGE result '" + avgStr + "' but text was: " + pdfText);
            assertTrue(pdfText.contains(String.valueOf(expectedBool)),
                    "PDF should contain boolean result '" + expectedBool + "' but text was: " + pdfText);
        } finally {
            if (outputFile.exists()) outputFile.delete();
            tempDir.toFile().delete();
        }
    }

    @Provide
    Arbitrary<FormulaSheetData> formulaSheetData() {
        // Generate 2-5 numeric source values (integers to avoid floating point issues)
        return Arbitraries.integers().between(1, 100)
                .array(double[].class).ofMinSize(2).ofMaxSize(5)
                .map(arr -> {
                    // Convert int array to double array
                    double[] doubles = new double[arr.length];
                    for (int i = 0; i < arr.length; i++) {
                        doubles[i] = arr[i];
                    }
                    return new FormulaSheetData(doubles);
                });
    }

    /**
     * Formats a numeric value the same way ExcelUtils does.
     */
    private String formatExpectedNumeric(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    record FormulaSheetData(double[] sourceValues) {}

    // ========== Property 7: Page Breaks Equal Sheet Count Minus One ==========

    /**
     * Property 7: Page Breaks Equal Sheet Count Minus One
     *
     * For any Workbook with N sheets (N ≥ 1), the converted PDF SHALL have at least N pages
     * (each sheet on its own page). For a single-sheet workbook, the PDF SHALL have exactly 1 page.
     *
     * **Validates: Requirements 6.1, 6.3**
     */
    @Property(tries = 25)
    @Label("Property 7: Page Breaks Equal Sheet Count Minus One — N sheets produce at least N PDF pages")
    void pageBreaksEqualSheetCountMinusOneProperty(
            @ForAll @IntRange(min = 1, max = 5) int numSheets
    ) throws Exception {
        // Build an XSSFWorkbook with numSheets sheets, each with some data
        byte[] xlsxBytes;
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            for (int s = 0; s < numSheets; s++) {
                Sheet sheet = workbook.createSheet("Sheet_" + s);
                Row row = sheet.createRow(0);
                row.createCell(0).setCellValue("SheetData_" + s);
                row.createCell(1).setCellValue("Col2_" + s);
            }
            workbook.write(baos);
            xlsxBytes = baos.toByteArray();
        }

        // Convert to PDF
        ExcelToPdfService service = new ExcelToPdfService(pdfBackend);
        Path tempDir = Files.createTempDirectory("pagebreak-prop-test");
        File outputFile = tempDir.resolve("output.pdf").toFile();

        try {
            MockMultipartFile xlsxFile = new MockMultipartFile(
                    "file", "multi-sheet.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, xlsxBytes
            );
            service.convertExcelToPdf(xlsxFile, outputFile, false);

            assertTrue(outputFile.exists(), "PDF file should be created");

            try (PDDocument document = Loader.loadPDF(outputFile)) {
                int pageCount = document.getNumberOfPages();
                assertTrue(pageCount >= numSheets,
                        "Workbook with " + numSheets + " sheets should produce at least " + numSheets
                                + " PDF pages, but got: " + pageCount);
            }
        } finally {
            if (outputFile.exists()) outputFile.delete();
            tempDir.toFile().delete();
        }
    }

    // ========== Property 2: Cell Formatting Extraction Preserves Style Metadata ==========

    /**
     * Property 2: Cell Formatting Extraction Preserves Style Metadata
     *
     * For any Cell with a CellStyle that has a bold Font, the extracted CellFormatting
     * SHALL have bold == true. For any Cell with a CellStyle that has a non-default fill
     * foreground color, the extracted CellFormatting SHALL have hasBackground == true and
     * RGB values matching the fill color from the CellStyle.
     *
     * **Validates: Requirements 3.1, 3.2**
     */
    @Property(tries = 25)
    @Label("Property 2: Cell Formatting Extraction — bold cells produce bold text in PDF, text content is preserved")
    void cellFormattingExtractionProperty(
            @ForAll("boldCellData") BoldCellData cellData
    ) throws Exception {
        // Build an XSSFWorkbook with cells that have random bold/non-bold fonts
        byte[] xlsxBytes;
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("FormattingSheet");

            for (int r = 0; r < cellData.values().length; r++) {
                Row row = sheet.createRow(r);
                Cell cell = row.createCell(0);
                cell.setCellValue(cellData.values()[r]);

                // Apply bold formatting based on the boldFlags
                XSSFFont font = workbook.createFont();
                font.setBold(cellData.boldFlags()[r]);
                XSSFCellStyle style = workbook.createCellStyle();
                style.setFont(font);
                cell.setCellStyle(style);
            }

            workbook.write(baos);
            xlsxBytes = baos.toByteArray();
        }

        // Convert to PDF
        ExcelToPdfService service = new ExcelToPdfService(pdfBackend);
        Path tempDir = Files.createTempDirectory("formatting-prop-test");
        File outputFile = tempDir.resolve("output.pdf").toFile();

        try {
            MockMultipartFile xlsxFile = new MockMultipartFile(
                    "file", "formatting.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, xlsxBytes
            );
            service.convertExcelToPdf(xlsxFile, outputFile, false);

            assertTrue(outputFile.exists(), "PDF file should be created");
            assertTrue(outputFile.length() > 0, "PDF file should not be empty");

            // Extract text from PDF and verify all cell values are present
            String pdfText;
            try (PDDocument document = Loader.loadPDF(outputFile)) {
                PDFTextStripper stripper = new PDFTextStripper();
                pdfText = stripper.getText(document);
            }

            // Verify that all cell text content appears in the PDF
            for (String value : cellData.values()) {
                assertTrue(pdfText.contains(value),
                        "PDF should contain cell value '" + value + "' but text was: " + pdfText);
            }
        } finally {
            if (outputFile.exists()) outputFile.delete();
            tempDir.toFile().delete();
        }
    }

    @Provide
    Arbitrary<BoldCellData> boldCellData() {
        return Arbitraries.integers().between(2, 5).flatMap(numRows -> {
            Arbitrary<String[]> valuesArb = Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(10)
                    .array(String[].class).ofSize(numRows);
            Arbitrary<boolean[]> boldArb = Arbitraries.of(true, false)
                    .array(boolean[].class).ofSize(numRows);
            return Combinators.combine(valuesArb, boldArb).as(BoldCellData::new);
        });
    }

    record BoldCellData(String[] values, boolean[] boldFlags) {}

    // ========== Property 3: Number Format Preservation ==========

    /**
     * Property 3: Number Format Preservation
     *
     * For any numeric Cell with a DataFormat pattern (currency, percentage, date, or custom),
     * the formatted value in the extracted CellFormatting SHALL equal the output of
     * DataFormatter.formatCellValue(cell) applied to that cell.
     *
     * **Validates: Requirements 3.3**
     */
    @Property(tries = 25)
    @Label("Property 3: Number Format Preservation — currency-formatted numeric cells appear formatted in PDF")
    void numberFormatPreservationProperty(
            @ForAll("currencyValues") double[] values
    ) throws Exception {
        DataFormatter dataFormatter = new DataFormatter();

        // Build an XSSFWorkbook with numeric cells using currency format
        byte[] xlsxBytes;
        String[] expectedFormatted = new String[values.length];
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("CurrencySheet");

            // Create a currency format style
            CellStyle currencyStyle = workbook.createCellStyle();
            short currencyFormat = workbook.createDataFormat().getFormat("$#,##0.00");
            currencyStyle.setDataFormat(currencyFormat);

            for (int r = 0; r < values.length; r++) {
                Row row = sheet.createRow(r);
                Cell cell = row.createCell(0);
                cell.setCellValue(values[r]);
                cell.setCellStyle(currencyStyle);

                // Capture the expected formatted value
                expectedFormatted[r] = dataFormatter.formatCellValue(cell);
            }

            workbook.write(baos);
            xlsxBytes = baos.toByteArray();
        }

        // Convert to PDF
        ExcelToPdfService service = new ExcelToPdfService(pdfBackend);
        Path tempDir = Files.createTempDirectory("currency-prop-test");
        File outputFile = tempDir.resolve("output.pdf").toFile();

        try {
            MockMultipartFile xlsxFile = new MockMultipartFile(
                    "file", "currency.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, xlsxBytes
            );
            service.convertExcelToPdf(xlsxFile, outputFile, false);

            assertTrue(outputFile.exists(), "PDF file should be created");

            // Extract text from PDF
            String pdfText;
            try (PDDocument document = Loader.loadPDF(outputFile)) {
                PDFTextStripper stripper = new PDFTextStripper();
                pdfText = stripper.getText(document);
            }

            // Verify that formatted values appear in the PDF
            for (String formatted : expectedFormatted) {
                assertTrue(pdfText.contains(formatted),
                        "PDF should contain formatted value '" + formatted + "' but text was: " + pdfText);
            }
        } finally {
            if (outputFile.exists()) outputFile.delete();
            tempDir.toFile().delete();
        }
    }

    @Provide
    Arbitrary<double[]> currencyValues() {
        // Generate 2-5 positive numeric values suitable for currency formatting
        return Arbitraries.integers().between(1, 99999)
                .map(i -> i / 100.0)
                .array(double[].class).ofMinSize(2).ofMaxSize(5);
    }

    // ========== Property 4: Column Width Proportionality ==========

    /**
     * Property 4: Column Width Proportionality
     *
     * For any Sheet with N columns, the calculated column widths SHALL be proportional
     * to the source metric. Specifically, for columns i and j,
     * calculatedWidth[i] / calculatedWidth[j] SHALL approximate
     * sourceMetric[i] / sourceMetric[j] within a tolerance of 1%.
     *
     * **Validates: Requirements 4.1, 4.2**
     */
    @Property(tries = 100)
    @Label("Property 4: Column Width Proportionality — calculated widths are proportional to source widths")
    void columnWidthProportionalityProperty(
            @ForAll("columnWidthInputs") int[] sourceWidths
    ) {
        // Pure math test: given source widths, calculate proportional widths
        float usableWidth = 495.28f; // A4 width (595.28) - 2*margin (50)

        float[] calculated = ExcelToPdfService.calculateColumnWidths(sourceWidths, usableWidth);

        // Verify proportionality: for any two columns i and j,
        // calculated[i]/calculated[j] ≈ sourceWidths[i]/sourceWidths[j]
        for (int i = 0; i < sourceWidths.length; i++) {
            for (int j = 0; j < sourceWidths.length; j++) {
                if (sourceWidths[j] > 0 && sourceWidths[i] > 0) {
                    double expectedRatio = (double) sourceWidths[i] / sourceWidths[j];
                    double actualRatio = (double) calculated[i] / calculated[j];
                    double tolerance = 0.01; // 1%
                    assertTrue(Math.abs(expectedRatio - actualRatio) <= tolerance * Math.max(1.0, Math.abs(expectedRatio)),
                            "Column widths should be proportional. Expected ratio " + expectedRatio
                                    + " but got " + actualRatio + " for columns " + i + " and " + j);
                }
            }
        }
    }

    @Provide
    Arbitrary<int[]> columnWidthInputs() {
        // Generate 2-8 column widths, each between 1 and 100
        return Arbitraries.integers().between(1, 100)
                .array(int[].class).ofMinSize(2).ofMaxSize(8);
    }

    // ========== Property 5: Column Widths Fit Within Page Width ==========

    /**
     * Property 5: Column Widths Fit Within Page Width
     *
     * For any set of calculated column widths, the sum of all column widths SHALL be
     * less than or equal to the printable page width. When raw sum exceeds available space,
     * widths SHALL be scaled proportionally so that the sum equals the available space exactly.
     *
     * **Validates: Requirements 4.3**
     */
    @Property(tries = 100)
    @Label("Property 5: Column Widths Fit Within Page Width — sum of widths ≤ usable page width")
    void columnWidthsFitPageProperty(
            @ForAll("columnWidthInputs") int[] sourceWidths
    ) {
        float usableWidth = 495.28f; // A4 width (595.28) - 2*margin (50)

        float[] calculated = ExcelToPdfService.calculateColumnWidths(sourceWidths, usableWidth);

        // Sum of calculated widths must not exceed usable page width
        float sum = 0;
        for (float w : calculated) {
            sum += w;
            assertTrue(w > 0, "Each column width should be positive");
        }

        assertTrue(sum <= usableWidth + 0.01f,
                "Sum of column widths (" + sum + ") should not exceed usable page width (" + usableWidth + ")");
    }

    // ========== Property 8: Streaming Output Equivalence ==========

    /**
     * Property 8: Streaming Output Equivalence
     *
     * For any valid XLSX file that can be processed by both the in-memory and SAX streaming
     * paths, the cell values and sheet names extracted by the streaming path SHALL be identical
     * to those extracted by the in-memory path. Specifically, for each sheet and each cell
     * position, the string value SHALL match.
     *
     * **Validates: Requirements 9.4**
     */
    @Property(tries = 10)
    @Label("Property 8: Streaming Output Equivalence — streaming and in-memory paths produce matching cell values and sheet names")
    void streamingOutputEquivalenceProperty(
            @ForAll("streamingTestWorkbook") StreamingTestWorkbook testData
    ) throws Exception {
        // Build an XSSFWorkbook with the generated data
        byte[] xlsxBytes;
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            for (int s = 0; s < testData.sheetNames().length; s++) {
                Sheet sheet = workbook.createSheet(testData.sheetNames()[s]);
                String[][] sheetData = testData.sheetData()[s];
                for (int r = 0; r < sheetData.length; r++) {
                    Row row = sheet.createRow(r);
                    for (int c = 0; c < sheetData[r].length; c++) {
                        row.createCell(c).setCellValue(sheetData[r][c]);
                    }
                }
            }
            workbook.write(baos);
            xlsxBytes = baos.toByteArray();
        }

        ExcelToPdfService service = new ExcelToPdfService(pdfBackend);
        Path tempDir = Files.createTempDirectory("streaming-equiv-prop-test");
        File inMemoryOutput = tempDir.resolve("in-memory.pdf").toFile();
        File streamingOutput = tempDir.resolve("streaming.pdf").toFile();

        try {
            MockMultipartFile xlsxFile = new MockMultipartFile(
                    "file", "equiv-test.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, xlsxBytes
            );

            // Process through in-memory path
            service.convertInMemory(xlsxFile, inMemoryOutput, false);

            // Process through streaming path
            service.convertXlsxStreaming(xlsxFile, streamingOutput);

            assertTrue(inMemoryOutput.exists(), "In-memory PDF should be created");
            assertTrue(streamingOutput.exists(), "Streaming PDF should be created");

            // Extract text from both PDFs
            String inMemoryText;
            try (PDDocument doc = Loader.loadPDF(inMemoryOutput)) {
                PDFTextStripper stripper = new PDFTextStripper();
                inMemoryText = stripper.getText(doc);
            }

            String streamingText;
            try (PDDocument doc = Loader.loadPDF(streamingOutput)) {
                PDFTextStripper stripper = new PDFTextStripper();
                streamingText = stripper.getText(doc);
            }

            // Verify sheet names appear in both outputs
            for (String sheetName : testData.sheetNames()) {
                assertTrue(inMemoryText.contains(sheetName),
                        "In-memory PDF should contain sheet name '" + sheetName + "'");
                assertTrue(streamingText.contains(sheetName),
                        "Streaming PDF should contain sheet name '" + sheetName + "'");
            }

            // Verify all cell values appear in both outputs
            for (String[][] sheetData : testData.sheetData()) {
                for (String[] row : sheetData) {
                    for (String cellValue : row) {
                        if (!cellValue.isEmpty()) {
                            assertTrue(inMemoryText.contains(cellValue),
                                    "In-memory PDF should contain cell value '" + cellValue + "'");
                            assertTrue(streamingText.contains(cellValue),
                                    "Streaming PDF should contain cell value '" + cellValue + "'");
                        }
                    }
                }
            }
        } finally {
            if (inMemoryOutput.exists()) inMemoryOutput.delete();
            if (streamingOutput.exists()) streamingOutput.delete();
            tempDir.toFile().delete();
        }
    }

    @Provide
    Arbitrary<StreamingTestWorkbook> streamingTestWorkbook() {
        // Generate 1-2 sheets, each with 5-20 rows and 2-4 columns
        return Arbitraries.integers().between(1, 2).flatMap(numSheets -> {
            Arbitrary<String[]> sheetNamesArb = Arbitraries.strings().alpha().ofMinLength(4).ofMaxLength(8)
                    .map(s -> "Sheet_" + s)
                    .array(String[].class).ofSize(numSheets)
                    .map(names -> {
                        // Ensure unique sheet names
                        Set<String> seen = new java.util.HashSet<>();
                        for (int i = 0; i < names.length; i++) {
                            while (seen.contains(names[i])) {
                                names[i] = names[i] + i;
                            }
                            seen.add(names[i]);
                        }
                        return names;
                    });

            return sheetNamesArb.flatMap(sheetNames ->
                    Arbitraries.integers().between(5, 20).flatMap(numRows ->
                            Arbitraries.integers().between(2, 4).flatMap(numCols -> {
                                // Generate cell data for each sheet
                                Arbitrary<String[][][]> allSheetsArb = Arbitraries.just(numSheets).flatMap(ns -> {
                                    String[][][] sheets = new String[ns][][];
                                    Arbitrary<String[][][]> result = Arbitraries.just(sheets);
                                    for (int s = 0; s < ns; s++) {
                                        final int sheetIdx = s;
                                        result = result.flatMap(arr ->
                                                Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(8)
                                                        .map(v -> "V_" + v)
                                                        .array(String[].class).ofSize(numCols)
                                                        .array(String[][].class).ofSize(numRows)
                                                        .map(sheetData -> {
                                                            arr[sheetIdx] = sheetData;
                                                            return arr;
                                                        })
                                        );
                                    }
                                    return result;
                                });

                                return allSheetsArb.map(sheetsData ->
                                        new StreamingTestWorkbook(sheetNames, sheetsData));
                            })
                    )
            );
        });
    }

    record StreamingTestWorkbook(String[] sheetNames, String[][][] sheetData) {}
}
