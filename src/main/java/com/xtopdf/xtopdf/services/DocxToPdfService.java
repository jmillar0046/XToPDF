package com.xtopdf.xtopdf.services;

import java.io.File;
import java.io.FileInputStream;
import java.util.Objects;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import com.itextpdf.io.exceptions.IOException;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;

public class DocxToPdfService {
   public void convertDocxToPdf(File docxFile, File pdfFile) throws IOException {
        // Read the DOCX file using Apache POI
        try (FileInputStream fis = new FileInputStream(docxFile)) {
            XWPFDocument docxDocument = new XWPFDocument(fis);
            // Create a PdfWriter and PdfDocument to write the PDF file
            try (PdfWriter writer = new PdfWriter(pdfFile)) {
                PdfDocument pdfDocument = new PdfDocument(writer);
                Document pdfDoc = new Document(pdfDocument);

                // Loop through paragraphs and add them to the PDF
                for (XWPFParagraph paragraph : docxDocument.getParagraphs()) {
                    processParagraph(paragraph, pdfDoc);
                }

                // Loop through tables and add them to the PDF
                for (XWPFTable table : docxDocument.getTables()) {
                    processTable(table, pdfDoc);
                }

                pdfDoc.close();
            }finally {
                docxDocument.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Error processing DOCX file: " + e.getMessage());
        }
    }

    void processParagraph(XWPFParagraph paragraph, Document pdfDoc) {
        // Create a new paragraph for each paragraph in DOCX
        Paragraph pdfParagraph = new Paragraph();

        // Add each run (formatted text) within the paragraph
        for (XWPFRun run : paragraph.getRuns()) {
            String text = run.getText(0);
            if (text != null) {
                // Create a new Text object and apply styles
                var pdfText = new Text(text);

                // Apply styles from the DOCX to the PDF
                if (run.isBold()) pdfText.setBold();
                if (run.isItalic()) pdfText.setItalic();
                if (Objects.nonNull(run.getUnderline())) pdfText.setUnderline();
                if (Objects.nonNull(run.getColor())) pdfText.setFontColor(getColorFromDocxColor(run.getColor()));
                if (run.getFontSizeAsDouble() > 0) pdfText.setFontSize(run.getFontSizeAsDouble().floatValue() * 0.75f); // Font size in PDF

                // Add text to the paragraph
                pdfParagraph.add(pdfText);
            }
        }

        // Add the paragraph to the PDF document
        pdfDoc.add(pdfParagraph);
    }

    void processTable(XWPFTable table, Document pdfDoc) {
        // Create a table for PDF from DOCX table
        var pdfTable = new Table(table.getRow(0).getTableCells().size());
        
        // Loop through rows and cells to add data to the table
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                pdfTable.addCell(cell.getText());
            }
        }

        // Add the table to the PDF document
        pdfDoc.add(pdfTable);
    }

    Color getColorFromDocxColor(String color) {
        var rgb = Integer.parseInt(color, 16);
        var red = (rgb >> 16) & 0xFF;
        var green = (rgb >> 8) & 0xFF;
        var blue = rgb & 0xFF;
        float[] colorValue = {red, green, blue};
        return Color.createColorWithColorSpace(colorValue);
    }
}
