package com.xtopdf.xtopdf.services.conversion.spreadsheet;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import com.xtopdf.xtopdf.utils.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

        // Route to appropriate conversion path (Requirements 9.1, 9.2)
        if (isStreamingRequired(excelFile)) {
            convertXlsxStreaming(excelFile, pdfFile);
        } else {
            convertInMemory(excelFile, pdfFile, executeMacros);
        }
    }

    /**
     * Determines whether the given file should be processed via SAX streaming.
     * Streaming is used for XLSX files exceeding the streaming threshold.
     * XLS (binary) files always use the in-memory path since Apache POI has
     * no SAX equivalent for the binary format.
     *
     * @param file the input file to check
     * @return true if streaming should be used
     */
    boolean isStreamingRequired(MultipartFile file) {
        if (file.getSize() <= STREAMING_THRESHOLD) {
            return false;
        }
        // Only XLSX supports SAX streaming — check by filename extension
        String filename = file.getOriginalFilename();
        if (filename != null && filename.toLowerCase().endsWith(".xlsx")) {
            return true;
        }
        return false;
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

    /**
     * SAX streaming conversion path for large XLSX files.
     * Uses OPCPackage + XSSFReader + SAX ContentHandler to parse the file
     * without loading the entire workbook into memory.
     *
     * <p>Note: The streaming path does NOT support cell formatting (CellFormatting)
     * — it only extracts plain text values. This is an acknowledged trade-off
     * per Requirement 9.4.</p>
     *
     * @param excelFile the input XLSX file
     * @param pdfFile   the output PDF file
     * @throws IOException if an I/O error occurs during conversion
     */
    void convertXlsxStreaming(MultipartFile excelFile, File pdfFile) throws IOException {
        try (InputStream inputStream = excelFile.getInputStream();
             OPCPackage opcPackage = OPCPackage.open(inputStream);
             PdfDocumentBuilder builder = pdfBackend.createBuilder()) {

            ReadOnlySharedStringsTable sharedStrings = new ReadOnlySharedStringsTable(opcPackage);
            XSSFReader xssfReader = new XSSFReader(opcPackage);

            Iterator<InputStream> sheetsData = xssfReader.getSheetsData();
            XSSFReader.SheetIterator sheetIterator = (XSSFReader.SheetIterator) sheetsData;

            boolean firstSheet = true;

            while (sheetIterator.hasNext()) {
                try (InputStream sheetStream = sheetIterator.next()) {
                    String sheetName = sheetIterator.getSheetName();

                    if (!firstSheet) {
                        builder.newPage();
                    }
                    firstSheet = false;

                    // Add sheet name as header
                    builder.addParagraph("Sheet: " + sheetName + "\n");

                    // Collect all rows from the SAX handler
                    List<List<String>> allRows = new ArrayList<>();
                    int[] maxColumns = {0};

                    SheetSaxHandler handler = new SheetSaxHandler(sharedStrings, (rowIndex, cellValues) -> {
                        // Pad the allRows list to accommodate sparse rows
                        while (allRows.size() <= rowIndex) {
                            allRows.add(null);
                        }
                        allRows.set(rowIndex, cellValues);
                        if (cellValues.size() > maxColumns[0]) {
                            maxColumns[0] = cellValues.size();
                        }
                    });

                    // Parse the sheet XML with SAX
                    SAXParserFactory factory = SAXParserFactory.newInstance();
                    SAXParser saxParser = factory.newSAXParser();
                    XMLReader xmlReader = saxParser.getXMLReader();
                    xmlReader.setContentHandler(handler);
                    xmlReader.parse(new InputSource(sheetStream));

                    // Write rows in chunks
                    if (allRows.isEmpty() || maxColumns[0] == 0) {
                        builder.addParagraph("(Empty sheet)\n");
                    } else {
                        writeRowsInChunks(allRows, maxColumns[0], builder);
                    }
                }
            }

            builder.save(pdfFile);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Error processing XLSX file in streaming mode: " + e.getMessage(), e);
        }
    }

    /**
     * Writes collected rows to the PDF builder in chunks of CHUNK_SIZE.
     * Each chunk is written as a separate table call.
     *
     * @param allRows    the collected rows (may contain nulls for sparse rows)
     * @param maxColumns the maximum number of columns across all rows
     * @param builder    the PDF document builder
     * @throws IOException if an I/O error occurs
     */
    private void writeRowsInChunks(List<List<String>> allRows, int maxColumns,
                                    PdfDocumentBuilder builder) throws IOException {
        int totalRows = allRows.size();
        for (int startRow = 0; startRow < totalRows; startRow += CHUNK_SIZE) {
            int endRow = Math.min(startRow + CHUNK_SIZE, totalRows);
            int chunkSize = endRow - startRow;

            String[][] chunkData = new String[chunkSize][maxColumns];
            for (int i = 0; i < chunkSize; i++) {
                List<String> rowValues = allRows.get(startRow + i);
                for (int col = 0; col < maxColumns; col++) {
                    if (rowValues != null && col < rowValues.size()) {
                        chunkData[i][col] = rowValues.get(col) != null ? rowValues.get(col) : "";
                    } else {
                        chunkData[i][col] = "";
                    }
                }
            }

            builder.addTable(chunkData);
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
        CellFormatting[][] formatting = extractFormatting(sheet, maxColumns);
        float[] columnWidths = calculateColumnWidths(sheet, maxColumns, tableData);
        builder.addTable(tableData, columnWidths, formatting);
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

    // ========== Cell Formatting Extraction (Requirements 3.1, 3.2, 3.3) ==========

    /**
     * Extracts cell formatting metadata for all cells in the sheet.
     * Reads CellStyle, Font, and uses DataFormatter for number format preservation.
     *
     * @param sheet      the sheet to extract formatting from
     * @param maxColumns the maximum number of columns
     * @return a 2D array of CellFormatting, one per cell
     */
    CellFormatting[][] extractFormatting(Sheet sheet, int maxColumns) {
        int rowCount = sheet.getLastRowNum() + 1;
        CellFormatting[][] formatting = new CellFormatting[rowCount][maxColumns];
        DataFormatter dataFormatter = new DataFormatter();
        Workbook workbook = sheet.getWorkbook();

        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                for (int cellIndex = 0; cellIndex < maxColumns; cellIndex++) {
                    formatting[rowIndex][cellIndex] = CellFormatting.PLAIN;
                }
            } else {
                for (int cellIndex = 0; cellIndex < maxColumns; cellIndex++) {
                    Cell cell = row.getCell(cellIndex);
                    formatting[rowIndex][cellIndex] = extractCellFormatting(cell, dataFormatter, workbook);
                }
            }
        }

        return formatting;
    }

    /**
     * Extracts formatting metadata from a single cell.
     */
    CellFormatting extractCellFormatting(Cell cell, DataFormatter dataFormatter, Workbook workbook) {
        if (cell == null) {
            return CellFormatting.PLAIN;
        }

        CellStyle style = cell.getCellStyle();
        if (style == null) {
            return CellFormatting.PLAIN;
        }

        // Extract bold flag from font
        boolean bold = false;
        try {
            Font font = workbook.getFontAt(style.getFontIndex());
            bold = font.getBold();
        } catch (Exception e) {
            log.debug("Could not extract font bold flag: {}", e.getMessage());
        }

        // Extract background color
        boolean hasBackground = false;
        int bgR = 255, bgG = 255, bgB = 255;
        try {
            Color fillColor = style.getFillForegroundColorColor();
            if (fillColor != null && style.getFillPattern() != FillPatternType.NO_FILL) {
                byte[] rgb = null;
                if (fillColor instanceof XSSFColor xssfColor) {
                    rgb = xssfColor.getRGB();
                    if (rgb == null) {
                        rgb = xssfColor.getARGBHex() != null ? hexToRgb(xssfColor.getARGBHex()) : null;
                    }
                } else if (fillColor instanceof org.apache.poi.hssf.util.HSSFColor hssfColor) {
                    short[] triplet = hssfColor.getTriplet();
                    if (triplet != null) {
                        rgb = new byte[]{(byte) triplet[0], (byte) triplet[1], (byte) triplet[2]};
                    }
                }
                if (rgb != null && rgb.length >= 3) {
                    bgR = rgb[0] & 0xFF;
                    bgG = rgb[1] & 0xFF;
                    bgB = rgb[2] & 0xFF;
                    // Only mark as having background if it's not white
                    if (bgR != 255 || bgG != 255 || bgB != 255) {
                        hasBackground = true;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Could not extract background color: {}", e.getMessage());
        }

        // Extract formatted value using DataFormatter
        // For FORMULA cells, skip DataFormatter since ExcelUtils handles formula evaluation better
        String formattedValue = null;
        try {
            if (cell.getCellType() != CellType.FORMULA) {
                String formatted = dataFormatter.formatCellValue(cell);
                if (formatted != null && !formatted.isEmpty()) {
                    formattedValue = formatted;
                }
            }
        } catch (Exception e) {
            log.debug("Could not format cell value: {}", e.getMessage());
        }

        return new CellFormatting(bold, bgR, bgG, bgB, hasBackground, formattedValue);
    }

    /**
     * Converts an ARGB hex string (e.g., "FF0000FF") to RGB byte array.
     */
    private byte[] hexToRgb(String argbHex) {
        if (argbHex == null || argbHex.length() < 6) {
            return null;
        }
        // Strip leading "FF" alpha if present (ARGB format)
        String rgb = argbHex.length() == 8 ? argbHex.substring(2) : argbHex;
        try {
            int r = Integer.parseInt(rgb.substring(0, 2), 16);
            int g = Integer.parseInt(rgb.substring(2, 4), 16);
            int b = Integer.parseInt(rgb.substring(4, 6), 16);
            return new byte[]{(byte) r, (byte) g, (byte) b};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ========== Column Width Calculation (Requirements 4.1, 4.2, 4.3) ==========

    /**
     * Calculates column widths for a sheet, proportional to the sheet's column width settings
     * or content length, scaled to fit within the printable page width.
     *
     * @param sheet      the sheet to calculate widths for
     * @param maxColumns the number of columns
     * @param data       the extracted table data (used for content-based fallback)
     * @return float array of column widths in points
     */
    float[] calculateColumnWidths(Sheet sheet, int maxColumns, String[][] data) {
        float usableWidth = 495.28f; // A4 (595.28) - 2 * 50pt margin

        int[] sourceWidths = new int[maxColumns];
        for (int col = 0; col < maxColumns; col++) {
            int sheetWidth = sheet.getColumnWidth(col);
            // Default column width in POI is 2048 (8 characters * 256)
            if (sheetWidth > 0 && sheetWidth != sheet.getDefaultColumnWidth() * 256) {
                // Use explicit sheet column width (in 1/256th of a character)
                sourceWidths[col] = sheetWidth;
            } else {
                // Fall back to max content length per column
                int maxLen = 1; // minimum 1 to avoid zero-width columns
                for (String[] row : data) {
                    if (col < row.length && row[col] != null) {
                        maxLen = Math.max(maxLen, row[col].length());
                    }
                }
                sourceWidths[col] = maxLen * 256; // Scale to same unit as POI column widths
            }
        }

        return calculateColumnWidths(sourceWidths, usableWidth);
    }

    /**
     * Pure math function: given source widths and usable page width,
     * calculates proportional column widths that fit within the page.
     * Package-private for property testing.
     *
     * @param sourceWidths array of source width values (any positive integers)
     * @param usableWidth  the available page width in points
     * @return float array of proportional column widths
     */
    static float[] calculateColumnWidths(int[] sourceWidths, float usableWidth) {
        float[] result = new float[sourceWidths.length];
        long totalSource = 0;
        for (int w : sourceWidths) {
            totalSource += Math.max(w, 1); // ensure no zero widths
        }

        if (totalSource == 0) {
            // Equal widths fallback
            float equalWidth = usableWidth / sourceWidths.length;
            for (int i = 0; i < result.length; i++) {
                result[i] = equalWidth;
            }
            return result;
        }

        for (int i = 0; i < sourceWidths.length; i++) {
            result[i] = (Math.max(sourceWidths[i], 1) / (float) totalSource) * usableWidth;
        }

        return result;
    }
}
