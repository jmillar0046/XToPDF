package com.xtopdf.xtopdf.services;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests using valid document structures created with Apache POI
 */
class ValidDocumentTests {

    @Test
    void testPptxToPdfService_ValidMinimalPptx() throws Exception {
        PptxToPdfService service = new PptxToPdfService();
        
        // Create a minimal valid PPTX
        try (XMLSlideShow ppt = new XMLSlideShow();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            XSLFSlide slide = ppt.createSlide();
            XSLFTextBox shape = slide.createTextBox();
            shape.setText("Test Slide");
            
            ppt.write(baos);
            byte[] pptxData = baos.toByteArray();
            
            MockMultipartFile file = new MockMultipartFile("test.pptx", "test.pptx", 
                "application/vnd.openxmlformats-officedocument.presentationml.presentation", pptxData);
            File output = new File(System.getProperty("java.io.tmpdir") + "/test_valid.pdf");
            
            service.convertPptxToPdf(file, output);
            
            assertTrue(output.exists());
            assertTrue(output.length() > 0);
            output.delete();
        }
    }

    @Test
    void testPptxToPdfService_WithBoldItalicText() throws Exception {
        PptxToPdfService service = new PptxToPdfService();
        
        // Create PPTX with formatted text
        try (XMLSlideShow ppt = new XMLSlideShow();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            XSLFSlide slide = ppt.createSlide();
            XSLFTextBox shape = slide.createTextBox();
            shape.setText("Bold and Italic Text");
            shape.getTextParagraphs().get(0).getTextRuns().get(0).setBold(true);
            shape.getTextParagraphs().get(0).getTextRuns().get(0).setItalic(true);
            
            ppt.write(baos);
            byte[] pptxData = baos.toByteArray();
            
            MockMultipartFile file = new MockMultipartFile("test.pptx", "test.pptx", 
                "application/vnd.openxmlformats-officedocument.presentationml.presentation", pptxData);
            File output = new File(System.getProperty("java.io.tmpdir") + "/test_formatted.pdf");
            
            service.convertPptxToPdf(file, output);
            
            assertTrue(output.exists());
            assertTrue(output.length() > 0);
            output.delete();
        }
    }

    @Test
    void testPptxToPdfService_WithFontSize() throws Exception {
        PptxToPdfService service = new PptxToPdfService();
        
        // Create PPTX with custom font size
        try (XMLSlideShow ppt = new XMLSlideShow();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            XSLFSlide slide = ppt.createSlide();
            XSLFTextBox shape = slide.createTextBox();
            shape.setText("Custom Font Size");
            shape.getTextParagraphs().get(0).getTextRuns().get(0).setFontSize(24.0);
            
            ppt.write(baos);
            byte[] pptxData = baos.toByteArray();
            
            MockMultipartFile file = new MockMultipartFile("test.pptx", "test.pptx", 
                "application/vnd.openxmlformats-officedocument.presentationml.presentation", pptxData);
            File output = new File(System.getProperty("java.io.tmpdir") + "/test_fontsize.pdf");
            
            service.convertPptxToPdf(file, output);
            
            assertTrue(output.exists());
            assertTrue(output.length() > 0);
            output.delete();
        }
    }

    @Test
    void testPptxToPdfService_MultipleSlides() throws Exception {
        PptxToPdfService service = new PptxToPdfService();
        
        // Create PPTX with multiple slides
        try (XMLSlideShow ppt = new XMLSlideShow();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            for (int i = 1; i <= 3; i++) {
                XSLFSlide slide = ppt.createSlide();
                XSLFTextBox shape = slide.createTextBox();
                shape.setText("Slide " + i);
            }
            
            ppt.write(baos);
            byte[] pptxData = baos.toByteArray();
            
            MockMultipartFile file = new MockMultipartFile("test.pptx", "test.pptx", 
                "application/vnd.openxmlformats-officedocument.presentationml.presentation", pptxData);
            File output = new File(System.getProperty("java.io.tmpdir") + "/test_multi.pdf");
            
            service.convertPptxToPdf(file, output);
            
            assertTrue(output.exists());
            assertTrue(output.length() > 0);
            output.delete();
        }
    }

    @Test
    void testXlsxToPdfService_ValidMinimalXlsx() throws Exception {
        XlsxToPdfService service = new XlsxToPdfService();
        
        // Create a minimal valid XLSX
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            XSSFSheet sheet = workbook.createSheet("Sheet1");
            XSSFRow row = sheet.createRow(0);
            XSSFCell cell = row.createCell(0);
            cell.setCellValue("Test Cell");
            
            workbook.write(baos);
            byte[] xlsxData = baos.toByteArray();
            
            MockMultipartFile file = new MockMultipartFile("test.xlsx", "test.xlsx", 
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", xlsxData);
            File output = new File(System.getProperty("java.io.tmpdir") + "/test_xlsx_valid.pdf");
            
            service.convertXlsxToPdf(file, output, false);
            
            assertTrue(output.exists());
            assertTrue(output.length() > 0);
            output.delete();
        }
    }

    @Test
    void testXlsxToPdfService_WithMultipleSheets() throws Exception {
        XlsxToPdfService service = new XlsxToPdfService();
        
        // Create XLSX with multiple sheets
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            for (int i = 1; i <= 3; i++) {
                XSSFSheet sheet = workbook.createSheet("Sheet" + i);
                XSSFRow row = sheet.createRow(0);
                XSSFCell cell = row.createCell(0);
                cell.setCellValue("Sheet " + i + " Data");
            }
            
            workbook.write(baos);
            byte[] xlsxData = baos.toByteArray();
            
            MockMultipartFile file = new MockMultipartFile("test.xlsx", "test.xlsx", 
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", xlsxData);
            File output = new File(System.getProperty("java.io.tmpdir") + "/test_xlsx_multi.pdf");
            
            service.convertXlsxToPdf(file, output, false);
            
            assertTrue(output.exists());
            assertTrue(output.length() > 0);
            output.delete();
        }
    }

    @Test
    void testXlsxToPdfService_WithExecuteMacros() throws Exception {
        XlsxToPdfService service = new XlsxToPdfService();
        
        // Create XLSX with formula
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            XSSFSheet sheet = workbook.createSheet("Sheet1");
            XSSFRow row = sheet.createRow(0);
            XSSFCell cell1 = row.createCell(0);
            cell1.setCellValue(10);
            XSSFCell cell2 = row.createCell(1);
            cell2.setCellValue(20);
            XSSFCell cell3 = row.createCell(2);
            cell3.setCellFormula("A1+B1");
            
            workbook.write(baos);
            byte[] xlsxData = baos.toByteArray();
            
            MockMultipartFile file = new MockMultipartFile("test.xlsx", "test.xlsx", 
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", xlsxData);
            File output = new File(System.getProperty("java.io.tmpdir") + "/test_xlsx_macros.pdf");
            
            service.convertXlsxToPdf(file, output, true);
            
            assertTrue(output.exists());
            assertTrue(output.length() > 0);
            output.delete();
        }
    }
}
