package com.xtopdf.xtopdf.utils;

import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verification test to ensure generated Excel test files are valid and contain expected content.
 */
@SpringBootTest
class ExcelTestFileVerificationTest {

    private static final String TEST_FILES_DIR = "src/test/resources/test-files/";

    @Test
    void verifyBasicSpreadsheet() throws Exception {
        File file = new File(TEST_FILES_DIR + "basic-spreadsheet.xlsx");
        assertTrue(file.exists(), "Basic spreadsheet file should exist");

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {
            
            assertNotNull(workbook, "Workbook should be readable");
            assertEquals(1, workbook.getNumberOfSheets(), "Should have 1 sheet");
            
            Sheet sheet = workbook.getSheetAt(0);
            assertEquals("Basic Data", sheet.getSheetName(), "Sheet name should be 'Basic Data'");
            
            // Verify header row
            Row headerRow = sheet.getRow(0);
            assertNotNull(headerRow, "Header row should exist");
            assertEquals("Name", headerRow.getCell(0).getStringCellValue());
            assertEquals("Age", headerRow.getCell(1).getStringCellValue());
            assertEquals("City", headerRow.getCell(2).getStringCellValue());
            assertEquals("Salary", headerRow.getCell(3).getStringCellValue());
            
            // Verify data rows exist
            assertTrue(sheet.getPhysicalNumberOfRows() >= 6, "Should have at least 6 rows (header + 5 data)");
            
            // Verify first data row
            Row dataRow = sheet.getRow(1);
            assertNotNull(dataRow, "First data row should exist");
            assertEquals("John Doe", dataRow.getCell(0).getStringCellValue());
            
            System.out.println("✓ Basic spreadsheet verified successfully");
        }
    }

    @Test
    void verifyFormulasSpreadsheet() throws Exception {
        File file = new File(TEST_FILES_DIR + "formulas-spreadsheet.xlsx");
        assertTrue(file.exists(), "Formulas spreadsheet file should exist");

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {
            
            assertNotNull(workbook, "Workbook should be readable");
            Sheet sheet = workbook.getSheetAt(0);
            assertEquals("Formulas", sheet.getSheetName(), "Sheet name should be 'Formulas'");
            
            // Verify formulas exist
            Row dataRow = sheet.getRow(1);
            assertNotNull(dataRow, "First data row should exist");
            
            Cell totalCell = dataRow.getCell(3);
            assertNotNull(totalCell, "Total cell should exist");
            assertEquals(CellType.FORMULA, totalCell.getCellType(), "Total cell should contain a formula");
            assertTrue(totalCell.getCellFormula().contains("*"), "Formula should contain multiplication");
            
            // Verify formula can be evaluated
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            CellValue cellValue = evaluator.evaluate(totalCell);
            assertNotNull(cellValue, "Formula should be evaluable");
            assertTrue(cellValue.getNumberValue() > 0, "Formula result should be positive");
            
            System.out.println("✓ Formulas spreadsheet verified successfully");
        }
    }

    @Test
    void verifyChartsSpreadsheet() throws Exception {
        File file = new File(TEST_FILES_DIR + "charts-spreadsheet.xlsx");
        assertTrue(file.exists(), "Charts spreadsheet file should exist");

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {
            
            assertNotNull(workbook, "Workbook should be readable");
            Sheet sheet = workbook.getSheetAt(0);
            assertEquals("Sales Data", sheet.getSheetName(), "Sheet name should be 'Sales Data'");
            
            // Verify data exists
            Row headerRow = sheet.getRow(0);
            assertNotNull(headerRow, "Header row should exist");
            assertEquals("Month", headerRow.getCell(0).getStringCellValue());
            assertEquals("Sales", headerRow.getCell(1).getStringCellValue());
            assertEquals("Expenses", headerRow.getCell(2).getStringCellValue());
            
            // Verify chart exists (charts are stored in drawing patriarch)
            assertNotNull(sheet.getDrawingPatriarch(), "Sheet should have drawings (chart)");
            
            System.out.println("✓ Charts spreadsheet verified successfully");
        }
    }

    @Test
    void verifyFormattedSpreadsheet() throws Exception {
        File file = new File(TEST_FILES_DIR + "formatted-spreadsheet.xlsx");
        assertTrue(file.exists(), "Formatted spreadsheet file should exist");

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {
            
            assertNotNull(workbook, "Workbook should be readable");
            Sheet sheet = workbook.getSheetAt(0);
            assertEquals("Formatted Data", sheet.getSheetName(), "Sheet name should be 'Formatted Data'");
            
            // Verify header formatting
            Row headerRow = sheet.getRow(0);
            assertNotNull(headerRow, "Header row should exist");
            Cell headerCell = headerRow.getCell(0);
            assertNotNull(headerCell, "Header cell should exist");
            
            CellStyle headerStyle = headerCell.getCellStyle();
            assertNotNull(headerStyle, "Header cell should have style");
            
            Font headerFont = workbook.getFontAt(headerStyle.getFontIndex());
            assertTrue(headerFont.getBold(), "Header font should be bold");
            
            // Verify currency formatting exists
            Row dataRow = sheet.getRow(1);
            Cell salaryCell = dataRow.getCell(2);
            assertNotNull(salaryCell, "Salary cell should exist");
            
            CellStyle salaryStyle = salaryCell.getCellStyle();
            assertNotNull(salaryStyle, "Salary cell should have style");
            assertTrue(salaryStyle.getDataFormatString().contains("$") || 
                      salaryStyle.getDataFormatString().contains("#,##0"),
                      "Salary should have currency or number format");
            
            // Verify merged regions exist
            assertTrue(sheet.getNumMergedRegions() > 0, "Sheet should have merged regions");
            
            System.out.println("✓ Formatted spreadsheet verified successfully");
        }
    }

    @Test
    void verifyCorruptedSpreadsheet() {
        File file = new File(TEST_FILES_DIR + "corrupted-spreadsheet.xlsx");
        assertTrue(file.exists(), "Corrupted spreadsheet file should exist");
        assertTrue(file.length() > 0, "Corrupted file should not be empty");

        // Verify that trying to read it throws an exception
        assertThrows(Exception.class, () -> {
            try (FileInputStream fis = new FileInputStream(file);
                 Workbook workbook = WorkbookFactory.create(fis)) {
                // Should not reach here
                fail("Should have thrown an exception for corrupted file");
            }
        }, "Reading corrupted file should throw an exception");
        
        System.out.println("✓ Corrupted spreadsheet verified successfully (correctly fails to parse)");
    }

    @Test
    void verifyAllTestFilesExist() {
        String[] expectedFiles = {
            "basic-spreadsheet.xlsx",
            "formulas-spreadsheet.xlsx",
            "charts-spreadsheet.xlsx",
            "formatted-spreadsheet.xlsx",
            "corrupted-spreadsheet.xlsx"
        };

        for (String fileName : expectedFiles) {
            File file = new File(TEST_FILES_DIR + fileName);
            assertTrue(file.exists(), fileName + " should exist");
            assertTrue(file.length() > 0, fileName + " should not be empty");
        }

        System.out.println("✓ All 5 test Excel files exist and are non-empty");
    }
}
