package com.xtopdf.xtopdf.services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import static org.mockito.Mockito.*;

public class DocxToPdfServiceTest {

    @Mock
    private XWPFDocument mockDocxDocument;

    @Mock
    private XWPFParagraph mockParagraph;

    @Mock
    private XWPFRun mockRun;

    @Mock
    private PdfWriter mockPdfWriter;

    @Mock
    private PdfDocument mockPdfDocument;

    @Mock
    private Document mockDocument;

    @InjectMocks
    private DocxToPdfService docxToPdfService;

    @BeforeEach
    void setUp() {
        docxToPdfService = new DocxToPdfService();
    }

    @Test
    void testConvertDocxToPdf() throws Exception {
        // Arrange
        File docxFile = mock(File.class);
        File pdfFile = mock(File.class);
        
        // Mock the DOCX document behavior
        when(mockDocxDocument.getParagraphs()).thenReturn(Collections.singletonList(mockParagraph));
        when(mockParagraph.getRuns()).thenReturn(Collections.singletonList(mockRun));
        when(mockRun.getText(0)).thenReturn("Sample text");
        when(mockRun.isBold()).thenReturn(true);  // Simulating bold text

        docxToPdfService.convertDocxToPdf(docxFile, pdfFile);

        // Verify that methods are called as expected
        verify(mockDocxDocument).getParagraphs();
        verify(mockParagraph).getRuns();
        verify(mockRun).getText(0);
        verify(mockRun).isBold();
        verify(mockDocument).add(any(Paragraph.class));
    }

    @Test
    void testConvertDocxToPdfWithEmptyFile() throws Exception {
        // Arrange
        File docxFile = mock(File.class);
        File pdfFile = mock(File.class);

        // Mock the DOCX document to return no paragraphs
        when(mockDocxDocument.getParagraphs()).thenReturn(Collections.emptyList());

        docxToPdfService.convertDocxToPdf(docxFile, pdfFile);

        // Verify that no paragraphs are processed
        verify(mockDocxDocument).getParagraphs();
        verify(mockParagraph, times(0)).getRuns();  // Shouldn't get any runs if there are no paragraphs
    }

    @Test
    void testConvertDocxToPdfHandlesException() {
        File docxFile = mock(File.class);
        File pdfFile = mock(File.class);

        // Simulate an error during file reading
        try {
            when(mockDocxDocument.getParagraphs()).thenThrow(new IOException("Mocked exception"));

            docxToPdfService.convertDocxToPdf(docxFile, pdfFile);

        } catch (Exception e) {
            assert(e instanceof IOException);
        }

        // Verify that exception was caught and logged
        verify(mockDocxDocument, times(1)).getParagraphs();
    }

    @Test
    void testDocumentWriting() throws Exception {
        File docxFile = new File("test.docx");
        File pdfFile = new File("test.pdf");

        // Mock behavior for document and PDF file
        when(mockDocxDocument.getParagraphs()).thenReturn(Collections.singletonList(mockParagraph));
        when(mockParagraph.getRuns()).thenReturn(Collections.singletonList(mockRun));
        when(mockRun.getText(0)).thenReturn("Test text");

        docxToPdfService.convertDocxToPdf(docxFile, pdfFile);

        // Verify that the PDF document writing methods are invoked
        verify(mockDocument).add(any(Paragraph.class));
    }
}
