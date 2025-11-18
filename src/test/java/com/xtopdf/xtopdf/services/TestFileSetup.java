package com.xtopdf.xtopdf.services;

import org.apache.poi.xslf.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.hslf.usermodel.*;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * This test creates binary test files in test resources for use by other tests
 */
class TestFileSetup {
    
    private static final String BASE_DIR = "src/test/resources/test-files/";
    
    @Test
    void createTestFiles() throws Exception {
        new File(BASE_DIR).mkdirs();
        
        createPptxFile();
        createXlsxFile();
        createXlsFile();
        createDocxFile();
        createPptFile();
        createBmpFile();
    }
    
    private void createPptxFile() throws Exception {
        try (XMLSlideShow pptx = new XMLSlideShow()) {
            // Slide 1
            XSLFSlide slide1 = pptx.createSlide();
            XSLFTextBox tb1 = slide1.createTextBox();
            tb1.setText("Test Slide 1");
            tb1.setAnchor(new java.awt.geom.Rectangle2D.Double(100, 100, 400, 100));
            
            // Slide 2 with formatting
            XSLFSlide slide2 = pptx.createSlide();
            XSLFTextBox tb2 = slide2.createTextBox();
            tb2.setText("Formatted Text");
            tb2.setAnchor(new java.awt.geom.Rectangle2D.Double(100, 100, 400, 100));
            tb2.getTextParagraphs().get(0).getTextRuns().get(0).setBold(true);
            tb2.getTextParagraphs().get(0).getTextRuns().get(0).setItalic(true);
            tb2.getTextParagraphs().get(0).getTextRuns().get(0).setFontSize(24.0);
            
            // Slide 3
            XSLFSlide slide3 = pptx.createSlide();
            XSLFTextBox tb3 = slide3.createTextBox();
            tb3.setText("Third Slide");
            tb3.setAnchor(new java.awt.geom.Rectangle2D.Double(100, 100, 400, 100));
            tb3.getTextParagraphs().get(0).getTextRuns().get(0).setBold(true);
            
            try (FileOutputStream out = new FileOutputStream(BASE_DIR + "test.pptx")) {
                pptx.write(out);
            }
        }
    }
    
    private void createXlsxFile() throws Exception {
        try (XSSFWorkbook xlsx = new XSSFWorkbook()) {
            // Sheet 1
            XSSFSheet sheet1 = xlsx.createSheet("Sheet1");
            XSSFRow row1 = sheet1.createRow(0);
            row1.createCell(0).setCellValue("Name");
            row1.createCell(1).setCellValue("Value");
            XSSFRow row2 = sheet1.createRow(1);
            row2.createCell(0).setCellValue("Item1");
            row2.createCell(1).setCellValue(100);
            
            // Sheet 2 with formula
            XSSFSheet sheet2 = xlsx.createSheet("Sheet2");
            XSSFRow row = sheet2.createRow(0);
            row.createCell(0).setCellValue(10);
            row.createCell(1).setCellValue(20);
            row.createCell(2).setCellFormula("A1+B1");
            
            try (FileOutputStream out = new FileOutputStream(BASE_DIR + "test.xlsx")) {
                xlsx.write(out);
            }
        }
    }
    
    private void createXlsFile() throws Exception {
        try (HSSFWorkbook xls = new HSSFWorkbook()) {
            HSSFSheet xlsSheet = xls.createSheet("Sheet1");
            HSSFRow xlsRow = xlsSheet.createRow(0);
            xlsRow.createCell(0).setCellValue("XLS Test");
            xlsRow.createCell(1).setCellValue(42);
            
            try (FileOutputStream out = new FileOutputStream(BASE_DIR + "test.xls")) {
                xls.write(out);
            }
        }
    }
    
    private void createDocxFile() throws Exception {
        try (XWPFDocument docx = new XWPFDocument()) {
            XWPFParagraph para1 = docx.createParagraph();
            XWPFRun run1 = para1.createRun();
            run1.setText("This is a test DOCX document.");
            run1.setBold(true);
            
            XWPFParagraph para2 = docx.createParagraph();
            XWPFRun run2 = para2.createRun();
            run2.setText("Second paragraph with italic text.");
            run2.setItalic(true);
            
            try (FileOutputStream out = new FileOutputStream(BASE_DIR + "test.docx")) {
                docx.write(out);
            }
        }
    }
    
    private void createPptFile() throws Exception {
        try (HSLFSlideShow ppt = new HSLFSlideShow()) {
            HSLFSlide pptSlide = ppt.createSlide();
            HSLFTextBox pptBox = pptSlide.createTextBox();
            pptBox.setText("PPT Test Slide");
            pptBox.setAnchor(new java.awt.geom.Rectangle2D.Double(100, 100, 400, 100));
            
            try (FileOutputStream out = new FileOutputStream(BASE_DIR + "test.ppt")) {
                ppt.write(out);
            }
        }
    }
    
    private void createBmpFile() throws Exception {
        BufferedImage bmpImg = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bmpImg.createGraphics();
        g2d.setColor(Color.BLUE);
        g2d.fillRect(0, 0, 100, 100);
        g2d.setColor(Color.WHITE);
        g2d.drawString("Test", 40, 50);
        g2d.dispose();
        ImageIO.write(bmpImg, "BMP", new File(BASE_DIR + "test.bmp"));
    }
}
