package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.services.conversion.document.DocxToPdfService;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class DocxToPdfServiceTest {
    private DocxToPdfService docxToPdfService;
    private com.xtopdf.xtopdf.pdf.PdfBackendProvider pdfBackend;

    @BeforeEach
    void setUp() {
        pdfBackend = new com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend();
        docxToPdfService = new DocxToPdfService(pdfBackend);
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

    // ---------------------------------------------------------------
    // Integration tests with PDFTextStripper content verification
    // ---------------------------------------------------------------

    @Test
    void testMixedContentOrderPreserved() throws Exception {
        // Create DOCX: paragraph → table → paragraph
        XWPFDocument document = new XWPFDocument();
        document.createParagraph().createRun().setText("Before table");
        var table = document.createTable(1, 2);
        table.getRow(0).getCell(0).setText("CellX");
        table.getRow(0).getCell(1).setText("CellY");
        document.createParagraph().createRun().setText("After table");

        byte[] docxBytes = toBytes(document);
        var docxFile = new MockMultipartFile("file", "mixed.docx", MediaType.APPLICATION_OCTET_STREAM_VALUE, docxBytes);
        var pdfFile = tempFile("mixed-content.pdf");

        docxToPdfService.convertDocxToPdf(docxFile, pdfFile);

        String pdfText = extractPdfText(pdfFile);
        int beforeIdx = pdfText.indexOf("Before table");
        int cellXIdx = pdfText.indexOf("CellX");
        int afterIdx = pdfText.indexOf("After table");

        assertTrue(beforeIdx >= 0, "PDF should contain 'Before table'");
        assertTrue(cellXIdx >= 0, "PDF should contain 'CellX'");
        assertTrue(afterIdx >= 0, "PDF should contain 'After table'");
        assertTrue(beforeIdx < cellXIdx, "'Before table' should appear before table content");
        assertTrue(cellXIdx < afterIdx, "Table content should appear before 'After table'");
    }

    @Test
    void testCyrillicCharactersRenderedWithoutQuestionMarks() throws Exception {
        XWPFDocument document = new XWPFDocument();
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText("Привет мир");

        byte[] docxBytes = toBytes(document);
        var docxFile = new MockMultipartFile("file", "cyrillic.docx", MediaType.APPLICATION_OCTET_STREAM_VALUE, docxBytes);
        var pdfFile = tempFile("cyrillic.pdf");

        docxToPdfService.convertDocxToPdf(docxFile, pdfFile);

        String pdfText = extractPdfText(pdfFile);
        assertTrue(pdfText.contains("Привет"), "PDF should contain Cyrillic text 'Привет'");
        assertTrue(pdfText.contains("мир"), "PDF should contain Cyrillic text 'мир'");
        assertFalse(pdfText.contains("?"), "PDF should not contain '?' placeholders for Cyrillic characters");
    }

    @Test
    void testEmptyDocxProducesValidPdf() throws Exception {
        XWPFDocument document = new XWPFDocument();
        byte[] docxBytes = toBytes(document);
        var docxFile = new MockMultipartFile("file", "empty-integration.docx", MediaType.APPLICATION_OCTET_STREAM_VALUE, docxBytes);
        var pdfFile = tempFile("empty-integration.pdf");

        docxToPdfService.convertDocxToPdf(docxFile, pdfFile);

        assertTrue(pdfFile.exists(), "PDF file should be created for empty DOCX");
        assertTrue(pdfFile.length() > 0, "PDF file should not be empty");
        // Verify it's a valid PDF by loading it
        try (PDDocument pdf = Loader.loadPDF(pdfFile)) {
            assertTrue(pdf.getNumberOfPages() >= 1, "PDF should have at least one page");
        }
    }

    @Test
    void testBoldItalicFormattingProducesValidPdfWithText() throws Exception {
        XWPFDocument document = new XWPFDocument();
        XWPFParagraph paragraph = document.createParagraph();

        XWPFRun boldRun = paragraph.createRun();
        boldRun.setText("Bold text ");
        boldRun.setBold(true);

        XWPFRun italicRun = paragraph.createRun();
        italicRun.setText("Italic text");
        italicRun.setItalic(true);

        byte[] docxBytes = toBytes(document);
        var docxFile = new MockMultipartFile("file", "bold-italic.docx", MediaType.APPLICATION_OCTET_STREAM_VALUE, docxBytes);
        var pdfFile = tempFile("bold-italic.pdf");

        docxToPdfService.convertDocxToPdf(docxFile, pdfFile);

        String pdfText = extractPdfText(pdfFile);
        assertTrue(pdfText.contains("Bold text"), "PDF should contain 'Bold text'");
        assertTrue(pdfText.contains("Italic text"), "PDF should contain 'Italic text'");
    }

    @Test
    void testMultipleParagraphsAndTableInterleaved() throws Exception {
        XWPFDocument document = new XWPFDocument();
        document.createParagraph().createRun().setText("Intro paragraph");

        var table1 = document.createTable(1, 2);
        table1.getRow(0).getCell(0).setText("T1R1C1");
        table1.getRow(0).getCell(1).setText("T1R1C2");

        document.createParagraph().createRun().setText("Middle paragraph");

        var table2 = document.createTable(1, 2);
        table2.getRow(0).getCell(0).setText("T2R1C1");
        table2.getRow(0).getCell(1).setText("T2R1C2");

        document.createParagraph().createRun().setText("Closing paragraph");

        byte[] docxBytes = toBytes(document);
        var docxFile = new MockMultipartFile("file", "interleaved.docx", MediaType.APPLICATION_OCTET_STREAM_VALUE, docxBytes);
        var pdfFile = tempFile("interleaved.pdf");

        docxToPdfService.convertDocxToPdf(docxFile, pdfFile);

        String pdfText = extractPdfText(pdfFile);
        int introIdx = pdfText.indexOf("Intro paragraph");
        int t1Idx = pdfText.indexOf("T1R1C1");
        int middleIdx = pdfText.indexOf("Middle paragraph");
        int t2Idx = pdfText.indexOf("T2R1C1");
        int closingIdx = pdfText.indexOf("Closing paragraph");

        assertTrue(introIdx >= 0, "PDF should contain 'Intro paragraph'");
        assertTrue(t1Idx >= 0, "PDF should contain table 1 content");
        assertTrue(middleIdx >= 0, "PDF should contain 'Middle paragraph'");
        assertTrue(t2Idx >= 0, "PDF should contain table 2 content");
        assertTrue(closingIdx >= 0, "PDF should contain 'Closing paragraph'");

        assertTrue(introIdx < t1Idx, "Intro should precede table 1");
        assertTrue(t1Idx < middleIdx, "Table 1 should precede middle paragraph");
        assertTrue(middleIdx < t2Idx, "Middle paragraph should precede table 2");
        assertTrue(t2Idx < closingIdx, "Table 2 should precede closing paragraph");
    }

    // ---------------------------------------------------------------
    // Helper methods
    // ---------------------------------------------------------------

    private byte[] toBytes(XWPFDocument document) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            document.write(baos);
            return baos.toByteArray();
        }
    }

    private File tempFile(String name) {
        return new File(System.getProperty("java.io.tmpdir") + "/" + name);
    }

    private String extractPdfText(File pdfFile) throws IOException {
        try (PDDocument pdf = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(pdf);
        }
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
