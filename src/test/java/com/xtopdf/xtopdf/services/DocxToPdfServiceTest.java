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
