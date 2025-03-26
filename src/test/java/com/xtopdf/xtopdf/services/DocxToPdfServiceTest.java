package com.xtopdf.xtopdf.services;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class DocxToPdfServiceTest {
    private DocxToPdfService docxToPdfService;

    @BeforeEach
    void setUp() {
        docxToPdfService = new DocxToPdfService();
    }

    @Test
    void testConvertDocxToPdf() throws Exception {
        var docxFile = new File("src/test/resources/test.docx");
        var pdfFile = new File("src/test/resources" + "/testOutput.pdf");

        docxToPdfService.convertDocxToPdf(docxFile, pdfFile);

        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");

    }

//    @Test
//    void testConvertDocxToPdfWithEmptyFile() throws Exception {
//        // Arrange
//        File docxFile = mock(File.class);
//        File pdfFile = mock(File.class);
//
//        // Mock the DOCX document to return no paragraphs
//        when(mockDocxDocument.getParagraphs()).thenReturn(Collections.emptyList());
//
//        docxToPdfService.convertDocxToPdf(docxFile, pdfFile);
//
//        // Verify that no paragraphs are processed
//        verify(mockDocxDocument).getParagraphs();
//        verify(mockParagraph, times(0)).getRuns();  // Shouldn't get any runs if there are no paragraphs
//    }
//
//    @Test
//    void testConvertDocxToPdfHandlesException() {
//        File docxFile = mock(File.class);
//        File pdfFile = mock(File.class);
//
//        // Simulate an error during file reading
//        try {
//            when(mockDocxDocument.getParagraphs()).thenThrow(new IOException("Mocked exception"));
//
//            docxToPdfService.convertDocxToPdf(docxFile, pdfFile);
//
//        } catch (Exception e) {
//            assert(e instanceof IOException);
//        }
//
//        // Verify that exception was caught and logged
//        verify(mockDocxDocument, times(1)).getParagraphs();
//    }
//
//    @Test
//    void testDocumentWriting() throws Exception {
//        File docxFile = new File("test.docx");
//        File pdfFile = new File("test.pdf");
//
//        // Mock behavior for document and PDF file
//        when(mockDocxDocument.getParagraphs()).thenReturn(Collections.singletonList(mockParagraph));
//        when(mockParagraph.getRuns()).thenReturn(Collections.singletonList(mockRun));
//        when(mockRun.getText(0)).thenReturn("Test text");
//
//        docxToPdfService.convertDocxToPdf(docxFile, pdfFile);
//
//        // Verify that the PDF document writing methods are invoked
//        verify(mockDocument).add(any(Paragraph.class));
//    }
}
