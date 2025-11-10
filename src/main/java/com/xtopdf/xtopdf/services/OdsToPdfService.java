package com.xtopdf.xtopdf.services;

import java.io.File;
import java.io.IOException;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;
import lombok.extern.slf4j.Slf4j;
import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;
import org.odftoolkit.odfdom.doc.table.OdfTable;
import org.odftoolkit.odfdom.doc.table.OdfTableCell;
import org.odftoolkit.odfdom.doc.table.OdfTableRow;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class OdsToPdfService {
    public void convertOdsToPdf(MultipartFile odsFile, File pdfFile) throws IOException {
        try (var fis = odsFile.getInputStream();
             PdfWriter writer = new PdfWriter(pdfFile)) {
            
            OdfSpreadsheetDocument odsDocument = OdfSpreadsheetDocument.loadDocument(fis);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document pdfDoc = new Document(pdfDocument);
            
            // Get list of tables (sheets)
            java.util.List<OdfTable> tables = odsDocument.getTableList();
            
            for (int i = 0; i < tables.size(); i++) {
                OdfTable table = tables.get(i);
                
                if (i > 0) {
                    pdfDoc.add(new Paragraph("\n"));
                }
                
                // Add sheet name as header
                Paragraph sheetHeader = new Paragraph("Sheet: " + table.getTableName());
                try {
                    sheetHeader.setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD));
                } catch (IOException ioException) {
                    // Fallback to regular font if bold font fails
                }
                pdfDoc.add(sheetHeader);
                
                processTable(table, pdfDoc);
            }
            
            pdfDoc.close();
        } catch (Exception e) {
            log.error("Error processing ODS file: {}", e.getMessage(), e);
            throw new IOException("Error processing ODS file: " + e.getMessage(), e);
        }
    }
    
    void processTable(OdfTable odfTable, Document pdfDoc) {
        int rowCount = odfTable.getRowCount();
        int columnCount = odfTable.getColumnCount();
        
        if (rowCount == 0 || columnCount == 0) {
            pdfDoc.add(new Paragraph("(Empty sheet)"));
            return;
        }
        
        // Create PDF table
        Table table = new Table(UnitValue.createPercentArray(columnCount)).useAllAvailableWidth();
        
        // Process each row and cell
        for (int row = 0; row < rowCount; row++) {
            OdfTableRow odfRow = odfTable.getRowByIndex(row);
            for (int col = 0; col < columnCount; col++) {
                OdfTableCell cell = odfRow.getCellByIndex(col);
                String cellValue = cell != null ? cell.getDisplayText() : "";
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(cellValue)));
            }
        }
        
        pdfDoc.add(table);
    }
}
