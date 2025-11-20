package com.xtopdf.xtopdf.utils;

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
