package com.xtopdf.xtopdf.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xddf.usermodel.chart.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Utility class to generate test Excel files with various features for testing.
 * This class creates Excel files that will be used by Excel service tests.
 */
public class ExcelTestFileGenerator {

    private static final String TEST_FILES_DIR = "src/test/resources/test-files/";

    public static void main(String[] args) {
        try {
            // Ensure directory exists
            File dir = new File(TEST_FILES_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            System.out.println("Generating test Excel files...");
            
            createBasicSpreadsheet();
            createSpreadsheetWithFormulas();
            createSpreadsheetWithCharts();
            createSpreadsheetWithFormatting();
            createCorruptedFile();
            
            System.out.println("All test Excel files generated successfully!");
            
        } catch (Exception e) {
            System.err.println("Error generating test files: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates a basic spreadsheet with simple data
     */
    private static void createBasicSpreadsheet() throws IOException {
        System.out.println("Creating basic spreadsheet...");
        
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Basic Data");

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Name");
            headerRow.createCell(1).setCellValue("Age");
            headerRow.createCell(2).setCellValue("City");
            headerRow.createCell(3).setCellValue("Salary");

            // Create data rows
            String[][] data = {
                {"John Doe", "30", "New York", "75000"},
                {"Jane Smith", "25", "Los Angeles", "65000"},
                {"Bob Johnson", "35", "Chicago", "80000"},
                {"Alice Williams", "28", "Houston", "70000"},
                {"Charlie Brown", "32", "Phoenix", "72000"}
            };

            for (int i = 0; i < data.length; i++) {
                Row row = sheet.createRow(i + 1);
                for (int j = 0; j < data[i].length; j++) {
                    Cell cell = row.createCell(j);
                    if (j == 1 || j == 3) {
                        // Numeric columns
                        cell.setCellValue(Double.parseDouble(data[i][j]));
                    } else {
                        cell.setCellValue(data[i][j]);
                    }
                }
            }

            // Auto-size columns
            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            // Save file
            try (FileOutputStream outputStream = new FileOutputStream(TEST_FILES_DIR + "basic-spreadsheet.xlsx")) {
                workbook.write(outputStream);
            }
            
            System.out.println("✓ Created: basic-spreadsheet.xlsx");
        }
    }

    /**
     * Creates a spreadsheet with various formulas
     */
    private static void createSpreadsheetWithFormulas() throws IOException {
        System.out.println("Creating spreadsheet with formulas...");
        
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Formulas");

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Product");
            headerRow.createCell(1).setCellValue("Quantity");
            headerRow.createCell(2).setCellValue("Price");
            headerRow.createCell(3).setCellValue("Total");
            headerRow.createCell(4).setCellValue("Tax (10%)");
            headerRow.createCell(5).setCellValue("Grand Total");

            // Create data rows with formulas
            String[][] products = {
                {"Laptop", "2", "1200"},
                {"Mouse", "5", "25"},
                {"Keyboard", "3", "75"},
                {"Monitor", "2", "300"}
            };

            for (int i = 0; i < products.length; i++) {
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(products[i][0]);
                row.createCell(1).setCellValue(Double.parseDouble(products[i][1]));
                row.createCell(2).setCellValue(Double.parseDouble(products[i][2]));
                
                // Formula: Quantity * Price
                Cell totalCell = row.createCell(3);
                totalCell.setCellFormula("B" + (i + 2) + "*C" + (i + 2));
                
                // Formula: Total * 0.1 (10% tax)
                Cell taxCell = row.createCell(4);
                taxCell.setCellFormula("D" + (i + 2) + "*0.1");
                
                // Formula: Total + Tax
                Cell grandTotalCell = row.createCell(5);
                grandTotalCell.setCellFormula("D" + (i + 2) + "+E" + (i + 2));
            }

            // Add summary row with SUM formulas
            Row summaryRow = sheet.createRow(products.length + 2);
            summaryRow.createCell(0).setCellValue("TOTAL:");
            summaryRow.createCell(3).setCellFormula("SUM(D2:D" + (products.length + 1) + ")");
            summaryRow.createCell(4).setCellFormula("SUM(E2:E" + (products.length + 1) + ")");
            summaryRow.createCell(5).setCellFormula("SUM(F2:F" + (products.length + 1) + ")");

            // Add average row
            Row avgRow = sheet.createRow(products.length + 3);
            avgRow.createCell(0).setCellValue("AVERAGE:");
            avgRow.createCell(3).setCellFormula("AVERAGE(D2:D" + (products.length + 1) + ")");

            // Auto-size columns
            for (int i = 0; i < 6; i++) {
                sheet.autoSizeColumn(i);
            }

            // Save file
            try (FileOutputStream outputStream = new FileOutputStream(TEST_FILES_DIR + "formulas-spreadsheet.xlsx")) {
                workbook.write(outputStream);
            }
            
            System.out.println("✓ Created: formulas-spreadsheet.xlsx");
        }
    }

    /**
     * Creates a spreadsheet with charts
     */
    private static void createSpreadsheetWithCharts() throws IOException {
        System.out.println("Creating spreadsheet with charts...");
        
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Sales Data");

            // Create data
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Month");
            headerRow.createCell(1).setCellValue("Sales");
            headerRow.createCell(2).setCellValue("Expenses");

            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun"};
            double[] sales = {15000, 18000, 22000, 19000, 25000, 28000};
            double[] expenses = {12000, 13000, 15000, 14000, 16000, 17000};

            for (int i = 0; i < months.length; i++) {
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(months[i]);
                row.createCell(1).setCellValue(sales[i]);
                row.createCell(2).setCellValue(expenses[i]);
            }

            // Create a drawing canvas
            XSSFDrawing drawing = sheet.createDrawingPatriarch();
            XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 4, 1, 14, 20);

            // Create a chart
            XSSFChart chart = drawing.createChart(anchor);
            chart.setTitleText("Monthly Sales vs Expenses");
            chart.setTitleOverlay(false);

            // Create data sources
            XDDFDataSource<String> categories = XDDFDataSourcesFactory.fromStringCellRange(sheet,
                    new CellRangeAddress(1, 6, 0, 0));
            XDDFNumericalDataSource<Double> salesData = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                    new CellRangeAddress(1, 6, 1, 1));
            XDDFNumericalDataSource<Double> expensesData = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                    new CellRangeAddress(1, 6, 2, 2));

            // Create chart axes
            XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
            bottomAxis.setTitle("Month");
            XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
            leftAxis.setTitle("Amount ($)");

            // Create line chart
            XDDFLineChartData data = (XDDFLineChartData) chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);
            
            XDDFLineChartData.Series series1 = (XDDFLineChartData.Series) data.addSeries(categories, salesData);
            series1.setTitle("Sales", null);
            series1.setSmooth(false);
            series1.setMarkerStyle(MarkerStyle.CIRCLE);

            XDDFLineChartData.Series series2 = (XDDFLineChartData.Series) data.addSeries(categories, expensesData);
            series2.setTitle("Expenses", null);
            series2.setSmooth(false);
            series2.setMarkerStyle(MarkerStyle.SQUARE);

            chart.plot(data);

            // Auto-size columns
            for (int i = 0; i < 3; i++) {
                sheet.autoSizeColumn(i);
            }

            // Save file
            try (FileOutputStream outputStream = new FileOutputStream(TEST_FILES_DIR + "charts-spreadsheet.xlsx")) {
                workbook.write(outputStream);
            }
            
            System.out.println("✓ Created: charts-spreadsheet.xlsx");
        }
    }

    /**
     * Creates a spreadsheet with various formatting
     */
    private static void createSpreadsheetWithFormatting() throws IOException {
        System.out.println("Creating spreadsheet with formatting...");
        
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Formatted Data");

            // Create various cell styles
            
            // Header style - Bold, centered, with background color
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 14);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Currency style
            CellStyle currencyStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            currencyStyle.setDataFormat(format.getFormat("$#,##0.00"));
            currencyStyle.setBorderBottom(BorderStyle.THIN);
            currencyStyle.setBorderTop(BorderStyle.THIN);
            currencyStyle.setBorderLeft(BorderStyle.THIN);
            currencyStyle.setBorderRight(BorderStyle.THIN);

            // Percentage style
            CellStyle percentStyle = workbook.createCellStyle();
            percentStyle.setDataFormat(format.getFormat("0.00%"));
            percentStyle.setBorderBottom(BorderStyle.THIN);
            percentStyle.setBorderTop(BorderStyle.THIN);
            percentStyle.setBorderLeft(BorderStyle.THIN);
            percentStyle.setBorderRight(BorderStyle.THIN);

            // Date style
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(format.getFormat("yyyy-mm-dd"));
            dateStyle.setBorderBottom(BorderStyle.THIN);
            dateStyle.setBorderTop(BorderStyle.THIN);
            dateStyle.setBorderLeft(BorderStyle.THIN);
            dateStyle.setBorderRight(BorderStyle.THIN);

            // Highlighted style (for important data)
            CellStyle highlightStyle = workbook.createCellStyle();
            highlightStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            highlightStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            highlightStyle.setFont(boldFont);
            highlightStyle.setBorderBottom(BorderStyle.THIN);
            highlightStyle.setBorderTop(BorderStyle.THIN);
            highlightStyle.setBorderLeft(BorderStyle.THIN);
            highlightStyle.setBorderRight(BorderStyle.THIN);

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(25);
            String[] headers = {"Employee", "Department", "Salary", "Bonus %", "Start Date"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Create data rows with formatting
            Object[][] data = {
                {"Alice Johnson", "Engineering", 95000.0, 0.15, "2020-01-15"},
                {"Bob Smith", "Marketing", 75000.0, 0.10, "2019-06-20"},
                {"Carol White", "Sales", 85000.0, 0.20, "2021-03-10"},
                {"David Brown", "Engineering", 105000.0, 0.15, "2018-11-05"},
                {"Eve Davis", "HR", 65000.0, 0.08, "2022-02-28"}
            };

            for (int i = 0; i < data.length; i++) {
                Row row = sheet.createRow(i + 1);
                
                // Employee name
                Cell nameCell = row.createCell(0);
                nameCell.setCellValue((String) data[i][0]);
                
                // Department
                Cell deptCell = row.createCell(1);
                deptCell.setCellValue((String) data[i][1]);
                
                // Salary with currency formatting
                Cell salaryCell = row.createCell(2);
                salaryCell.setCellValue((Double) data[i][2]);
                salaryCell.setCellStyle(currencyStyle);
                
                // Bonus percentage
                Cell bonusCell = row.createCell(3);
                bonusCell.setCellValue((Double) data[i][3]);
                bonusCell.setCellStyle(percentStyle);
                
                // Start date
                Cell dateCell = row.createCell(4);
                dateCell.setCellValue((String) data[i][4]);
                dateCell.setCellStyle(dateStyle);
            }

            // Add a summary row with highlighting
            Row summaryRow = sheet.createRow(data.length + 2);
            Cell summaryLabelCell = summaryRow.createCell(0);
            summaryLabelCell.setCellValue("Average Salary:");
            summaryLabelCell.setCellStyle(highlightStyle);
            
            Cell summaryValueCell = summaryRow.createCell(2);
            summaryValueCell.setCellFormula("AVERAGE(C2:C" + (data.length + 1) + ")");
            summaryValueCell.setCellStyle(currencyStyle);

            // Merge cells for title
            sheet.addMergedRegion(new CellRangeAddress(data.length + 4, data.length + 4, 0, 4));
            Row titleRow = sheet.createRow(data.length + 4);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Employee Compensation Report - Q1 2024");
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setColor(IndexedColors.DARK_GREEN.getIndex());
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleCell.setCellStyle(titleStyle);

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Save file
            try (FileOutputStream outputStream = new FileOutputStream(TEST_FILES_DIR + "formatted-spreadsheet.xlsx")) {
                workbook.write(outputStream);
            }
            
            System.out.println("✓ Created: formatted-spreadsheet.xlsx");
        }
    }

    /**
     * Creates a corrupted Excel file for error testing
     */
    private static void createCorruptedFile() throws IOException {
        System.out.println("Creating corrupted file...");
        
        // Create a file with .xlsx extension but invalid content
        File corruptedFile = new File(TEST_FILES_DIR + "corrupted-spreadsheet.xlsx");
        try (FileOutputStream fos = new FileOutputStream(corruptedFile)) {
            // Write invalid data that looks like it might be an Excel file but isn't
            String invalidContent = "PK\u0003\u0004This is not a valid Excel file content. " +
                    "It has the ZIP signature at the start but corrupted data after that. " +
                    "This should cause parsing errors when trying to read it as an Excel file.";
            fos.write(invalidContent.getBytes());
        }
        
        System.out.println("✓ Created: corrupted-spreadsheet.xlsx");
    }
}
