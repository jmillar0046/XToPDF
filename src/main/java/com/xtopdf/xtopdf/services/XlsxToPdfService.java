package com.xtopdf.xtopdf.services;

import java.io.File;
import java.io.IOException;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class XlsxToPdfService {
    
    public void convertXlsxToPdf(MultipartFile xlsxFile, File pdfFile) throws IOException {
        try (var fis = xlsxFile.getInputStream();
             Workbook workbook = new XSSFWorkbook(fis);
             PdfWriter writer = new PdfWriter(pdfFile)) {
            
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document pdfDoc = new Document(pdfDocument);
            
            // Process each sheet in the workbook
            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                
                if (sheetIndex > 0) {
                    pdfDoc.add(new Paragraph("\n")); // Add space between sheets
                }
                
                // Add sheet name as header
                Paragraph sheetHeader = new Paragraph("Sheet: " + sheet.getSheetName());
                try {
                    sheetHeader.setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD));
                } catch (IOException ioException) {
                    // Fallback to regular font if bold font fails
                }
                pdfDoc.add(sheetHeader);
                
                processSheet(sheet, pdfDoc);
            }
            
            pdfDoc.close();
        } catch (Exception e) {
            throw new IOException("Error processing XLSX file: " + e.getMessage(), e);
        }
    }
    
    void processSheet(Sheet sheet, Document pdfDoc) {
        if (sheet.getPhysicalNumberOfRows() == 0) {
            pdfDoc.add(new Paragraph("(Empty sheet)"));
            return;
        }
        
        // Determine the maximum number of columns
        int maxColumns = 0;
        for (Row row : sheet) {
            if (row.getLastCellNum() > maxColumns) {
                maxColumns = row.getLastCellNum();
            }
        }
        
        if (maxColumns == 0) {
            pdfDoc.add(new Paragraph("(No data in sheet)"));
            return;
        }
        
        // Create table with appropriate number of columns
        Table table = new Table(UnitValue.createPercentArray(maxColumns)).useAllAvailableWidth();
        
        // Process each row
        for (Row row : sheet) {
            for (int cellIndex = 0; cellIndex < maxColumns; cellIndex++) {
                org.apache.poi.ss.usermodel.Cell cell = row.getCell(cellIndex);
                String cellValue = getCellValueAsString(cell);
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(cellValue)));
            }
        }
        
        pdfDoc.add(table);
    }
    
    String getCellValueAsString(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                // Check if it's a date
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // Format numeric values to avoid scientific notation for integers
                    double numValue = cell.getNumericCellValue();
                    if (numValue == Math.floor(numValue)) {
                        return String.valueOf((long) numValue);
                    } else {
                        return String.valueOf(numValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    // Try to get the cached formula result
                    switch (cell.getCachedFormulaResultType()) {
                        case STRING:
                            return cell.getStringCellValue();
                        case NUMERIC:
                            double numValue = cell.getNumericCellValue();
                            if (numValue == Math.floor(numValue)) {
                                return String.valueOf((long) numValue);
                            } else {
                                return String.valueOf(numValue);
                            }
                        case BOOLEAN:
                            return String.valueOf(cell.getBooleanCellValue());
                        default:
                            return cell.getCellFormula();
                    }
                } catch (Exception e) {
                    return cell.getCellFormula();
                }
            case BLANK:
            default:
                return "";
        }
    }
}