package com.xtopdf.xtopdf.services;


import com.itextpdf.io.source.ByteArrayOutputStream;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class DocxToPdfServiceTest {
    private DocxToPdfService docxToPdfService;

    @BeforeEach
    void setUp() {
        docxToPdfService = new DocxToPdfService();
    }

    @Test
    void testConvertDocxToPdf() throws Exception {
        var content = "Hello, this is a test file content!";
        var docxFile = new MockMultipartFile("file", "test.docx", MediaType.APPLICATION_OCTET_STREAM_VALUE, createMockDocxFileContent());
        var pdfFile = new File(System.getProperty("java.io.tmpdir") + "/testOutput.pdf");

        docxToPdfService.convertDocxToPdf(docxFile, pdfFile);

        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
    }

    @Test
    void testConvertDocxWithMultipleParagraphs() throws Exception {
        XWPFDocument document = new XWPFDocument();
        document.createParagraph().createRun().setText("First paragraph.");
        document.createParagraph().createRun().setText("Second paragraph.");
        byte[] docxBytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            document.write(baos);
            docxBytes = baos.toByteArray();
        }
        var docxFile = new MockMultipartFile("file", "multi.docx", MediaType.APPLICATION_OCTET_STREAM_VALUE, docxBytes);
        var pdfFile = new File(System.getProperty("java.io.tmpdir") + "/multiParagraphs.pdf");
        docxToPdfService.convertDocxToPdf(docxFile, pdfFile);
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertDocxWithTable() throws Exception {
        XWPFDocument document = new XWPFDocument();
        var table = document.createTable(2, 2);
        table.getRow(0).getCell(0).setText("A1");
        table.getRow(0).getCell(1).setText("B1");
        table.getRow(1).getCell(0).setText("A2");
        table.getRow(1).getCell(1).setText("B2");
        byte[] docxBytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            document.write(baos);
            docxBytes = baos.toByteArray();
        }
        var docxFile = new MockMultipartFile("file", "table.docx", MediaType.APPLICATION_OCTET_STREAM_VALUE, docxBytes);
        var pdfFile = new File(System.getProperty("java.io.tmpdir") + "/table.pdf");
        docxToPdfService.convertDocxToPdf(docxFile, pdfFile);
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertDocxWithStyledText() throws Exception {
        XWPFDocument document = new XWPFDocument();
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText("Bold and Italic");
        run.setBold(true);
        run.setItalic(true);
        run.setUnderline(org.apache.poi.xwpf.usermodel.UnderlinePatterns.SINGLE);
        run.setColor("FF0000");
        run.setFontSize(20);
        byte[] docxBytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            document.write(baos);
            docxBytes = baos.toByteArray();
        }
        var docxFile = new MockMultipartFile("file", "styled.docx", MediaType.APPLICATION_OCTET_STREAM_VALUE, docxBytes);
        var pdfFile = new File(System.getProperty("java.io.tmpdir") + "/styled.pdf");
        docxToPdfService.convertDocxToPdf(docxFile, pdfFile);
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertEmptyDocx() throws Exception {
        XWPFDocument document = new XWPFDocument();
        byte[] docxBytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            document.write(baos);
            docxBytes = baos.toByteArray();
        }
        var docxFile = new MockMultipartFile("file", "empty.docx", MediaType.APPLICATION_OCTET_STREAM_VALUE, docxBytes);
        var pdfFile = new File(System.getProperty("java.io.tmpdir") + "/empty.pdf");
        docxToPdfService.convertDocxToPdf(docxFile, pdfFile);
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertCorruptedDocxThrows() {
        byte[] corrupted = new byte[] {0, 1, 2, 3, 4, 5};
        var docxFile = new MockMultipartFile("file", "corrupt.docx", MediaType.APPLICATION_OCTET_STREAM_VALUE, corrupted);
        var pdfFile = new File(System.getProperty("java.io.tmpdir") + "/corrupt.pdf");
        try {
            docxToPdfService.convertDocxToPdf(docxFile, pdfFile);
            assertTrue(false, "Should throw IOException for corrupted DOCX");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("Error processing DOCX file"));
        }
    }

    @Test
    void testConvertDocxWithUnicodeCharacters() throws Exception {
        XWPFDocument document = new XWPFDocument();
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText("Unicode: 你好, мир, hello!");
        byte[] docxBytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            document.write(baos);
            docxBytes = baos.toByteArray();
        }
        var docxFile = new MockMultipartFile("file", "unicode.docx", MediaType.APPLICATION_OCTET_STREAM_VALUE, docxBytes);
        var pdfFile = new File(System.getProperty("java.io.tmpdir") + "/unicode.pdf");
        docxToPdfService.convertDocxToPdf(docxFile, pdfFile);
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertDocxWithBoldOnlyText() throws Exception {
        XWPFDocument document = new XWPFDocument();
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText("Bold only text");
        run.setBold(true);
        run.setItalic(false);
        byte[] docxBytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            document.write(baos);
            docxBytes = baos.toByteArray();
        }
        var docxFile = new MockMultipartFile("file", "bold-only.docx", MediaType.APPLICATION_OCTET_STREAM_VALUE, docxBytes);
        var pdfFile = new File(System.getProperty("java.io.tmpdir") + "/bold-only.pdf");
        docxToPdfService.convertDocxToPdf(docxFile, pdfFile);
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertDocxWithItalicOnlyText() throws Exception {
        XWPFDocument document = new XWPFDocument();
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText("Italic only text");
        run.setBold(false);
        run.setItalic(true);
        byte[] docxBytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            document.write(baos);
            docxBytes = baos.toByteArray();
        }
        var docxFile = new MockMultipartFile("file", "italic-only.docx", MediaType.APPLICATION_OCTET_STREAM_VALUE, docxBytes);
        var pdfFile = new File(System.getProperty("java.io.tmpdir") + "/italic-only.pdf");
        docxToPdfService.convertDocxToPdf(docxFile, pdfFile);
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    private byte[] createMockDocxFileContent() throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            XWPFDocument document = new XWPFDocument();

            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText("Hello, this is a mock DOCX file.");

            // Write the document to the byte array output stream
            document.write(baos);

            return baos.toByteArray();
        }
    }
}
