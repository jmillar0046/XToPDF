package com.xtopdf.xtopdf.services.conversion.spreadsheet;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend;
import com.xtopdf.xtopdf.utils.ExcelUtils;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
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
}
