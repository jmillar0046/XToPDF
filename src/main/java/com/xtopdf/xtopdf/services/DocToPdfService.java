package com.xtopdf.xtopdf.services;

import java.io.File;
import java.io.IOException;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.TableIterator;
import org.apache.poi.hwpf.usermodel.TableRow;
import org.apache.poi.hwpf.usermodel.TableCell;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class DocToPdfService {
    public void convertDocToPdf(MultipartFile docFile, File pdfFile) throws IOException {
        try (var fis = docFile.getInputStream();
             HWPFDocument docDocument = new HWPFDocument(fis);
             PdfWriter writer = new PdfWriter(pdfFile)) {
            
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document pdfDoc = new Document(pdfDocument);
            
            // Get the range of the document
            Range range = docDocument.getRange();
            
            // Extract text from the document
            String text = range.text();
            
            // Add text as paragraphs
            if (text != null && !text.isEmpty()) {
                String[] paragraphs = text.split("\r");
                for (String para : paragraphs) {
                    if (!para.trim().isEmpty()) {
                        pdfDoc.add(new Paragraph(para));
                    }
                }
            }
            
            // Process tables if any
            TableIterator tableIterator = new TableIterator(range);
            while (tableIterator.hasNext()) {
                org.apache.poi.hwpf.usermodel.Table table = (org.apache.poi.hwpf.usermodel.Table) tableIterator.next();
                processTable(table, pdfDoc);
            }
            
            pdfDoc.close();
        } catch (Exception e) {
            log.error("Error processing DOC file: {}", e.getMessage(), e);
            throw new IOException("Error processing DOC file: " + e.getMessage(), e);
        }
    }
    
    void processTable(org.apache.poi.hwpf.usermodel.Table table, Document pdfDoc) {
        if (table.numRows() > 0) {
            TableRow firstRow = table.getRow(0);
            int numCols = firstRow.numCells();
            
            Table pdfTable = new Table(numCols);
            
            for (int rowIndex = 0; rowIndex < table.numRows(); rowIndex++) {
                TableRow row = table.getRow(rowIndex);
                for (int cellIndex = 0; cellIndex < row.numCells(); cellIndex++) {
                    TableCell cell = row.getCell(cellIndex);
                    String cellText = cell.text().trim();
                    pdfTable.addCell(cellText);
                }
            }
            
            pdfDoc.add(pdfTable);
        }
    }
}
