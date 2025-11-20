package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * Service to convert XLSX (Excel) files to PDF.
 * Uses Apache POI to parse XLSX and PDFBox to generate PDF.
 */
@Service
public class XlsxToPdfService {
    
    private final PdfBackendProvider pdfBackend;
    
    public XlsxToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }
    
    public void convertXlsxToPdf(MultipartFile xlsxFile, File pdfFile) throws IOException {
        convertXlsxToPdf(xlsxFile, pdfFile, false);
    }
    
    public void convertXlsxToPdf(MultipartFile xlsxFile, File pdfFile, boolean executeMacros) throws IOException {
        try (var fis = xlsxFile.getInputStream();
             Workbook workbook = new XSSFWorkbook(fis);
             PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
            
            // Process each sheet in the workbook
            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                
                if (sheetIndex > 0) {
                    builder.addParagraph("\n"); // Add space between sheets
                }
                
                // Add sheet name as header
                builder.addParagraph("Sheet: " + sheet.getSheetName() + "\n");
                
                processSheet(sheet, builder);
            }
            
            builder.save(pdfFile);
        } catch (Exception e) {
            throw new IOException("Error processing XLSX file: " + e.getMessage(), e);
        }
    }
    
    private void processSheet(Sheet sheet, PdfDocumentBuilder builder) throws IOException {
        if (sheet.getPhysicalNumberOfRows() == 0) {
            builder.addParagraph("(Empty sheet)\n");
            return;
        }
        
        int maxColumns = getMaxColumnCount(sheet);
        
        if (maxColumns == 0) {
            builder.addParagraph("(No data in sheet)\n");
            return;
        }
        
        String[][] tableData = extractTableData(sheet, maxColumns);
        builder.addTable(tableData);
    }
    
    private int getMaxColumnCount(Sheet sheet) {
        int maxColumns = 0;
        for (Row row : sheet) {
            if (row.getLastCellNum() > maxColumns) {
                maxColumns = row.getLastCellNum();
            }
        }
        return maxColumns;
    }
    
    private String[][] extractTableData(Sheet sheet, int maxColumns) {
        int rowCount = sheet.getPhysicalNumberOfRows();
        String[][] data = new String[rowCount][maxColumns];
        
        int rowIndex = 0;
        for (Row row : sheet) {
            for (int cellIndex = 0; cellIndex < maxColumns; cellIndex++) {
                Cell cell = row.getCell(cellIndex);
                data[rowIndex][cellIndex] = getCellValueAsString(cell);
            }
            rowIndex++;
        }
        
        return data;
    }
    
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    return sdf.format(cell.getDateCellValue());
                } else {
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return getCellFormulaResult(cell);
                } catch (Exception e) {
                    return cell.getCellFormula();
                }
            case BLANK:
            default:
                return "";
        }
    }
    
    private String getCellFormulaResult(Cell cell) {
        try {
            switch (cell.getCachedFormulaResultType()) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                default:
                    return "";
            }
        } catch (Exception e) {
            return "";
        }
    }
}
