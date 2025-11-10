package com.xtopdf.xtopdf.services;

import java.io.File;
import java.io.IOException;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;
import com.xtopdf.xtopdf.utils.ExcelUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class XlsxToPdfService {
    
    public void convertXlsxToPdf(MultipartFile xlsxFile, File pdfFile) throws IOException {
        convertXlsxToPdf(xlsxFile, pdfFile, false);
    }
    
    public void convertXlsxToPdf(MultipartFile xlsxFile, File pdfFile, boolean executeMacros) throws IOException {
        try (var fis = xlsxFile.getInputStream();
             Workbook workbook = new XSSFWorkbook(fis);
             PdfWriter writer = new PdfWriter(pdfFile)) {
            
            // Recalculate all formulas if macro execution is enabled
            if (executeMacros) {
                ExcelUtils.recalculateFormulas(workbook);
            }
            
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
                
                ExcelUtils.processSheet(sheet, pdfDoc);
            }
            
            pdfDoc.close();
        } catch (Exception e) {
            throw new IOException("Error processing XLSX file: " + e.getMessage(), e);
        }
    }
}