package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import lombok.extern.slf4j.Slf4j;
import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;
import org.odftoolkit.odfdom.doc.table.OdfTable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class OdsToPdfService {
    
    private final PdfBackendProvider pdfBackend;
    
    public OdsToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }
    
    public void convertOdsToPdf(MultipartFile odsFile, File pdfFile) throws IOException {
        try (var fis = odsFile.getInputStream();
             PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
            
            OdfSpreadsheetDocument odsDocument = OdfSpreadsheetDocument.loadDocument(fis);
            List<OdfTable> tables = odsDocument.getTableList();
            
            for (int i = 0; i < tables.size(); i++) {
                OdfTable table = tables.get(i);
                
                if (i > 0) {
                    builder.addParagraph("\n");
                }
                
                builder.addParagraph("Sheet: " + table.getTableName() + "\n");
                
                int rowCount = table.getRowCount();
                int colCount = table.getColumnCount();
                
                if (rowCount == 0 || colCount == 0) {
                    builder.addParagraph("(Empty sheet)\n");
                    continue;
                }
                
                String[][] tableData = new String[rowCount][colCount];
                
                for (int r = 0; r < rowCount; r++) {
                    for (int c = 0; c < colCount; c++) {
                        try {
                            String cellValue = table.getCellByPosition(c, r).getDisplayText();
                            tableData[r][c] = cellValue != null ? cellValue : "";
                        } catch (Exception e) {
                            tableData[r][c] = "";
                        }
                    }
                }
                
                builder.addTable(tableData);
            }
            
            builder.save(pdfFile);
        } catch (Exception e) {
            log.error("Error processing ODS file: {}", e.getMessage(), e);
            throw new IOException("Error processing ODS file: " + e.getMessage(), e);
        }
    }
}
