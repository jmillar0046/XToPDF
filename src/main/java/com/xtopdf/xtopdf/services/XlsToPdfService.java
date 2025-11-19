package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

@Service
public class XlsToPdfService {
    
    private final PdfBackendProvider pdfBackend;
    
    public XlsToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }
    
    public void convertXlsToPdf(MultipartFile xlsFile, File pdfFile) throws IOException {
        convertXlsToPdf(xlsFile, pdfFile, false);
    }
    
    public void convertXlsToPdf(MultipartFile xlsFile, File pdfFile, boolean executeMacros) throws IOException {
        try (var fis = xlsFile.getInputStream();
             Workbook workbook = new HSSFWorkbook(fis);
             PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
            
            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                
                if (sheetIndex > 0) {
                    builder.addParagraph("\n");
                }
                
                builder.addParagraph("Sheet: " + sheet.getSheetName() + "\n");
                
                if (sheet.getPhysicalNumberOfRows() == 0) {
                    builder.addParagraph("(Empty sheet)\n");
                    continue;
                }
                
                int maxColumns = 0;
                for (Row row : sheet) {
                    if (row.getLastCellNum() > maxColumns) {
                        maxColumns = row.getLastCellNum();
                    }
                }
                
                if (maxColumns == 0) {
                    builder.addParagraph("(No data in sheet)\n");
                    continue;
                }
                
                String[][] tableData = new String[sheet.getPhysicalNumberOfRows()][maxColumns];
                int rowIndex = 0;
                for (Row row : sheet) {
                    for (int cellIndex = 0; cellIndex < maxColumns; cellIndex++) {
                        Cell cell = row.getCell(cellIndex);
                        tableData[rowIndex][cellIndex] = getCellValue(cell);
                    }
                    rowIndex++;
                }
                
                builder.addTable(tableData);
            }
            
            builder.save(pdfFile);
        } catch (Exception e) {
            throw new IOException("Error processing XLS file: " + e.getMessage(), e);
        }
    }
    
    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    return sdf.format(cell.getDateCellValue());
                } else {
                    double val = cell.getNumericCellValue();
                    return val == (long)val ? String.valueOf((long)val) : String.valueOf(val);
                }
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return getCellValue(cell); // Recursive for formula result
                } catch (Exception e) {
                    return cell.getCellFormula();
                }
            default: return "";
        }
    }
}
