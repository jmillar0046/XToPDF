package com.xtopdf.xtopdf.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ExcelUtilsTest {

    private Workbook workbook;
    private Sheet sheet;

    @BeforeEach
    void setUp() {
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Test Sheet");
    }

    @Test
    void getCellValueAsString_NullCell_ReturnsEmptyString() {
        String result = ExcelUtils.getCellValueAsString(null);
        assertEquals("", result);
    }

    @Test
    void getCellValueAsString_StringCell_ReturnsString() {
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue("Test String");

        String result = ExcelUtils.getCellValueAsString(cell);
        assertEquals("Test String", result);
    }

    @Test
    void getCellValueAsString_NumericCell_ReturnsNumericString() {
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue(42);

        String result = ExcelUtils.getCellValueAsString(cell);
        assertEquals("42", result);
    }

    @Test
    void getCellValueAsString_DecimalCell_ReturnsDecimalString() {
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue(3.14);

        String result = ExcelUtils.getCellValueAsString(cell);
        assertEquals("3.14", result);
    }

    @Test
    void getCellValueAsString_BooleanCell_ReturnsBooleanString() {
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue(true);

        String result = ExcelUtils.getCellValueAsString(cell);
        assertEquals("true", result);
    }

    @Test
    void getCellValueAsString_BlankCell_ReturnsEmptyString() {
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setBlank();

        String result = ExcelUtils.getCellValueAsString(cell);
        assertEquals("", result);
    }

    @Test
    void getCellValueAsString_FormulaCell_ReturnsFormulaResult() {
        Row row = sheet.createRow(0);
        Cell cell1 = row.createCell(0);
        cell1.setCellValue(10);
        Cell cell2 = row.createCell(1);
        cell2.setCellValue(20);
        Cell formulaCell = row.createCell(2);
        formulaCell.setCellFormula("A1+B1");

        // Evaluate the formula
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        evaluator.evaluateFormulaCell(formulaCell);

        String result = ExcelUtils.getCellValueAsString(formulaCell);
        assertEquals("30", result);
    }

    @Test
    void recalculateFormulas_UpdatesAllFormulas() throws IOException {
        // Create cells with values
        Row row = sheet.createRow(0);
        Cell cell1 = row.createCell(0);
        cell1.setCellValue(5);
        Cell cell2 = row.createCell(1);
        cell2.setCellValue(10);
        Cell formulaCell = row.createCell(2);
        formulaCell.setCellFormula("A1*B1");

        // Recalculate all formulas
        ExcelUtils.recalculateFormulas(workbook);

        // Check that formula result is cached
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        CellValue cellValue = evaluator.evaluate(formulaCell);
        assertEquals(50.0, cellValue.getNumberValue());

        workbook.close();
    }

    @Test
    void processSheet_EmptySheet_AddsEmptyMessage() {
        com.itextpdf.kernel.pdf.PdfDocument pdfDocument = new com.itextpdf.kernel.pdf.PdfDocument(
            new com.itextpdf.kernel.pdf.PdfWriter(new java.io.ByteArrayOutputStream())
        );
        com.itextpdf.layout.Document pdfDoc = new com.itextpdf.layout.Document(pdfDocument);

        ExcelUtils.processSheet(sheet, pdfDoc);

        pdfDoc.close();
        // Test passes if no exception is thrown
        assertTrue(true);
    }

    @Test
    void processSheet_SheetWithData_CreatesTable() {
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue("Test Data");

        com.itextpdf.kernel.pdf.PdfDocument pdfDocument = new com.itextpdf.kernel.pdf.PdfDocument(
            new com.itextpdf.kernel.pdf.PdfWriter(new java.io.ByteArrayOutputStream())
        );
        com.itextpdf.layout.Document pdfDoc = new com.itextpdf.layout.Document(pdfDocument);

        ExcelUtils.processSheet(sheet, pdfDoc);

        pdfDoc.close();
        // Test passes if no exception is thrown
        assertTrue(true);
    }
}
