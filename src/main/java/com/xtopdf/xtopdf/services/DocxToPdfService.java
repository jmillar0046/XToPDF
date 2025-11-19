package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Service to convert DOCX (Word) files to PDF.
 * Uses Apache POI to parse DOCX and PDFBox to generate PDF.
 * 
 * Note: This implementation extracts text and table content.
 * Rich formatting (bold, italic, colors, fonts) is simplified.
 */
@Service
@Slf4j
public class DocxToPdfService {
    
    private final PdfBackendProvider pdfBackend;
    
    public DocxToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }
    
    public void convertDocxToPdf(MultipartFile docxFile, File pdfFile) throws IOException {
        try (var fis = docxFile.getInputStream();
             XWPFDocument docxDocument = new XWPFDocument(fis);
             PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
            
            // Process paragraphs
            for (XWPFParagraph paragraph : docxDocument.getParagraphs()) {
                String text = paragraph.getText();
                if (text != null && !text.trim().isEmpty()) {
                    builder.addParagraph(text);
                }
            }
            
            // Process tables
            for (XWPFTable table : docxDocument.getTables()) {
                processTable(table, builder);
            }
            
            builder.save(pdfFile);
        } catch (Exception e) {
            log.error("Error processing DOCX file: {}", e.getMessage(), e);
            throw new IOException("Error processing DOCX file: " + e.getMessage());
        }
    }
    
    private void processTable(XWPFTable table, PdfDocumentBuilder builder) throws IOException {
        if (table.getRows().isEmpty()) {
            return;
        }
        
        // Determine table dimensions
        int numCols = table.getRow(0).getTableCells().size();
        int numRows = table.getRows().size();
        
        String[][] tableData = new String[numRows][numCols];
        
        // Extract table data
        for (int i = 0; i < numRows; i++) {
            XWPFTableRow row = table.getRow(i);
            for (int j = 0; j < numCols && j < row.getTableCells().size(); j++) {
                XWPFTableCell cell = row.getCell(j);
                tableData[i][j] = cell.getText();
            }
        }
        
        builder.addTable(tableData);
    }
}
