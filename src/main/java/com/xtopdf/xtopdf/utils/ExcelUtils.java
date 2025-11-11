package com.xtopdf.xtopdf.utils;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import org.apache.poi.ss.usermodel.*;

/**
 * Utility class for Excel to PDF conversion operations.
 * Provides common functionality for processing Excel workbooks and sheets.
 */
public final class ExcelUtils {
    
    private ExcelUtils() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Processes the given Excel sheet and adds its contents to the provided PDF document.
     * <p>
     * If the sheet is empty, a message indicating an empty sheet is added to the PDF.
     * If the sheet has no data (no columns), a message indicating no data is added.
     * Otherwise, the sheet's data is rendered as a table in the PDF.
     *
     * @param sheet   the Excel sheet to process
     * @param pdfDoc  the PDF document to which the sheet's contents will be added
     */
    public static void processSheet(Sheet sheet, Document pdfDoc) {
        if (sheet.getPhysicalNumberOfRows() == 0) {
            pdfDoc.add(new Paragraph("(Empty sheet)"));
            return;
        }
        
        int maxColumns = getMaxColumnCount(sheet);
        
        if (maxColumns == 0) {
            pdfDoc.add(new Paragraph("(No data in sheet)"));
            return;
        }
        
        Table table = createTable(sheet, maxColumns);
        pdfDoc.add(table);
    }
    
    /**
     * Determines the maximum number of columns in a sheet.
     *
     * @param sheet the Excel sheet to analyze
     * @return the maximum number of columns
     */
    private static int getMaxColumnCount(Sheet sheet) {
        int maxColumns = 0;
        for (Row row : sheet) {
            if (row.getLastCellNum() > maxColumns) {
                maxColumns = row.getLastCellNum();
            }
        }
        return maxColumns;
    }
    
    /**
     * Creates a PDF table from an Excel sheet.
     *
     * @param sheet      the Excel sheet to convert
     * @param maxColumns the number of columns in the table
     * @return a PDF table containing the sheet data
     */
    private static Table createTable(Sheet sheet, int maxColumns) {
        Table table = new Table(UnitValue.createPercentArray(maxColumns)).useAllAvailableWidth();
        
        for (Row row : sheet) {
            for (int cellIndex = 0; cellIndex < maxColumns; cellIndex++) {
                Cell cell = row.getCell(cellIndex);
                String cellValue = getCellValueAsString(cell);
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(cellValue)));
            }
        }
        
        return table;
    }
    
    /**
     * Converts the given Excel cell to its string representation.
     * <p>
     * Handles the following cell types:
     * <ul>
     *   <li><b>STRING</b>: Returns the cell's string value.</li>
     *   <li><b>NUMERIC</b>: Returns the numeric value as a string. If the cell is date-formatted, returns the date as a string.
     *       For integer values, returns without decimal point; otherwise, returns the double value as a string.</li>
     *   <li><b>BOOLEAN</b>: Returns "true" or "false".</li>
     *   <li><b>FORMULA</b>: Attempts to return the cached formula result as a string, handling STRING, NUMERIC, and BOOLEAN results.
     *       If unable to evaluate, returns the formula itself as a string.</li>
     *   <li><b>BLANK</b> or unknown types: Returns an empty string.</li>
     * </ul>
     * If the cell is {@code null}, returns an empty string.
     *
     * @param cell the Excel cell to convert
     * @return the string representation of the cell's value, or an empty string if the cell is null or blank
     */
    public static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return formatNumericCell(cell);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return getFormulaResult(cell);
            case BLANK:
            default:
                return "";
        }
    }
    
    /**
     * Formats a numeric cell value as a string.
     * Handles both regular numbers and date-formatted cells.
     *
     * @param cell the numeric cell to format
     * @return the formatted string representation
     */
    private static String formatNumericCell(Cell cell) {
        if (DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue().toString();
        }
        return formatNumericValue(cell.getNumericCellValue());
    }
    
    /**
     * Formats a numeric value, avoiding scientific notation for integers.
     *
     * @param numValue the numeric value to format
     * @return the formatted string
     */
    private static String formatNumericValue(double numValue) {
        if (numValue == Math.floor(numValue) && !Double.isInfinite(numValue)) {
            return String.valueOf((long) numValue);
        }
        return String.valueOf(numValue);
    }
    
    /**
     * Gets the result of a formula cell as a string.
     *
     * @param cell the formula cell
     * @return the formula result or the formula itself if evaluation fails
     */
    private static String getFormulaResult(Cell cell) {
        try {
            switch (cell.getCachedFormulaResultType()) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    return formatNumericValue(cell.getNumericCellValue());
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                default:
                    return cell.getCellFormula();
            }
        } catch (Exception e) {
            return cell.getCellFormula();
        }
    }
    
    /**
     * Recalculates all formulas in the workbook.
     * This is useful for ensuring formula results are up-to-date before conversion,
     * especially for formulas that may reference external data or user-defined functions.
     *
     * @param workbook the workbook to recalculate
     */
    public static void recalculateFormulas(Workbook workbook) {
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        evaluator.evaluateAll();
    }
}
