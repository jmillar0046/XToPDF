package com.xtopdf.xtopdf.services.conversion.spreadsheet;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import com.xtopdf.xtopdf.utils.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Unified service for converting Excel files (both XLS and XLSX) to PDF.
 * Uses Apache POI's WorkbookFactory for format auto-detection and delegates
 * cell extraction to ExcelUtils.
 */
@Slf4j
@Service
public class ExcelToPdfService {

    static final long MAX_FILE_SIZE = 100_000_000L; // 100 MB
    static final long STREAMING_THRESHOLD = 10_000_000L; // 10 MB
    static final int CHUNK_SIZE = 1000;

    private final PdfBackendProvider pdfBackend;

    public ExcelToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }

    /**
     * Converts an Excel file (XLS or XLSX) to PDF.
     * Overload that defaults executeMacros to false.
     *
     * @param excelFile the input Excel file
     * @param pdfFile   the output PDF file
     * @throws IOException if an I/O error occurs during conversion
     */
    public void convertExcelToPdf(MultipartFile excelFile, File pdfFile) throws IOException {
        convertExcelToPdf(excelFile, pdfFile, false);
    }

    /**
     * Converts an Excel file (XLS or XLSX) to PDF.
     *
     * @param excelFile     the input Excel file
     * @param pdfFile       the output PDF file
     * @param executeMacros if true, recalculate formulas before conversion
     * @throws IOException              if an I/O error occurs during conversion
     * @throws IllegalArgumentException if excelFile or pdfFile is null
     */
    public void convertExcelToPdf(MultipartFile excelFile, File pdfFile,
                                   boolean executeMacros) throws IOException {
        // Validate inputs before any resource allocation (Requirement 8.4)
        if (excelFile == null) {
            throw new IllegalArgumentException("Input file must not be null");
        }
        if (pdfFile == null) {
            throw new IllegalArgumentException("Output file must not be null");
        }
        if (excelFile.getSize() > MAX_FILE_SIZE) {
            throw new IOException("File size exceeds maximum allowed: " + MAX_FILE_SIZE + " bytes");
        }

        // Route to appropriate conversion path
        convertInMemory(excelFile, pdfFile, executeMacros);
    }

    /**
     * In-memory conversion path using WorkbookFactory for format auto-detection.
     * Handles both XLS and XLSX files below the streaming threshold.
     */
    void convertInMemory(MultipartFile excelFile, File pdfFile,
                         boolean executeMacros) throws IOException {
        try (var inputStream = excelFile.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream);
             PdfDocumentBuilder builder = pdfBackend.createBuilder()) {

            // Recalculate formulas when executeMacros is true (Requirement 2.1)
            if (executeMacros) {
                try {
                    ExcelUtils.recalculateFormulas(workbook);
                } catch (Exception e) {
                    log.warn("Formula recalculation failed, continuing with cached values: {}", e.getMessage());
                }
            }

            // Process each sheet in the workbook
            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);

                if (sheetIndex > 0) {
                    builder.newPage();
                }

                // Add sheet name as header
                builder.addParagraph("Sheet: " + sheet.getSheetName() + "\n");

                processSheet(sheet, builder);
            }

            builder.save(pdfFile);
        } catch (IllegalArgumentException e) {
            // Re-throw validation exceptions as-is
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Error processing Excel file: " + e.getMessage(), e);
        }
    }

    private void processSheet(Sheet sheet, PdfDocumentBuilder builder) throws IOException {
        if (sheet.getPhysicalNumberOfRows() == 0) {
            builder.addParagraph("(Empty sheet)\n");
            return;
        }

        int maxColumns = getMaxColumnCount(sheet);

        if (maxColumns == 0) {
            builder.addParagraph("(Empty sheet)\n");
            return;
        }

        String[][] tableData = extractTableData(sheet, maxColumns);
        builder.addTable(tableData);
    }

    private int getMaxColumnCount(Sheet sheet) {
        int maxColumns = 0;
        int totalRows = sheet.getLastRowNum() + 1;
        for (int i = 0; i < totalRows; i++) {
            Row row = sheet.getRow(i);
            if (row != null && row.getLastCellNum() > maxColumns) {
                maxColumns = row.getLastCellNum();
            }
        }
        return maxColumns;
    }

    private String[][] extractTableData(Sheet sheet, int maxColumns) {
        int rowCount = sheet.getLastRowNum() + 1;
        String[][] data = new String[rowCount][maxColumns];

        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                // Fill missing rows with empty strings
                for (int cellIndex = 0; cellIndex < maxColumns; cellIndex++) {
                    data[rowIndex][cellIndex] = "";
                }
            } else {
                for (int cellIndex = 0; cellIndex < maxColumns; cellIndex++) {
                    Cell cell = row.getCell(cellIndex);
                    data[rowIndex][cellIndex] = ExcelUtils.getCellValueAsString(cell);
                }
            }
        }

        return data;
    }
}
