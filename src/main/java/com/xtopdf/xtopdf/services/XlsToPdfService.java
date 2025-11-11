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
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class XlsToPdfService {
    
    public void convertXlsToPdf(MultipartFile xlsFile, File pdfFile) throws IOException {
        convertXlsToPdf(xlsFile, pdfFile, false);
    }
    
    public void convertXlsToPdf(MultipartFile xlsFile, File pdfFile, boolean executeMacros) throws IOException {
        try (var fis = xlsFile.getInputStream();
             Workbook workbook = new HSSFWorkbook(fis);
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
            throw new IOException("Error processing XLS file: " + e.getMessage(), e);
        }
    }
}
