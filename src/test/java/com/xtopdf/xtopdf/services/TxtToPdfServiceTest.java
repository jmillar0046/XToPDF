package com.xtopdf.xtopdf.services;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.itextpdf.kernel.pdf.PdfWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import java.io.*;

class TxtToPdfServiceTest {

    private TxtToPdfService txtToPdfService;

    @Mock
    private File txtFile;

    @Mock
    private File pdfFile;

    @Mock
    private BufferedReader bufferedReader;

    @BeforeEach
    void setUp() {
        txtToPdfService = new TxtToPdfService();
    }

    @Test
    void testConvertTxtToPdf_Success() throws Exception {
        // Arrange
        String txtContent = "Hello, this is a test file.";
        when(txtFile.exists()).thenReturn(true);
        when(txtFile.isFile()).thenReturn(true);

        // Mock reading the content of the .txt file
        when(bufferedReader.readLine()).thenReturn(txtContent).thenReturn(null); // Simulate reading line by line

        // Act
        // Call the method under test
        File outputPdfFile = new File(System.getProperty("java.io.tmpdir") + "/testOutput.pdf");
        txtToPdfService.convertTxtToPdf(txtFile, outputPdfFile);

        // Assert
        assertTrue(outputPdfFile.exists(), "The PDF file should be created.");
        assertTrue(outputPdfFile.length() > 0, "The PDF file should not be empty.");
    }

    @Test
    void testConvertTxtToPdf_FileNotFound() {
        // Arrange
        when(txtFile.exists()).thenReturn(false);

        // Act & Assert
        assertThrows(IOException.class, () -> {
            txtToPdfService.convertTxtToPdf(txtFile, pdfFile);
        });
    }

    @Test
    void testConvertTxtToPdf_EmptyFile() throws Exception {
        // Arrange
        when(txtFile.exists()).thenReturn(true);
        when(txtFile.isFile()).thenReturn(true);

        // Mock reading an empty content from .txt file
        when(bufferedReader.readLine()).thenReturn(null); // Simulate no content

        // Act
        File outputPdfFile = new File(System.getProperty("java.io.tmpdir") + "/testEmptyOutput.pdf");
        txtToPdfService.convertTxtToPdf(txtFile, outputPdfFile);

        // Assert
        assertTrue(outputPdfFile.exists(), "The PDF file should be created.");
        assertEquals(0, outputPdfFile.length(), "The PDF file should be empty if the input text file is empty.");
    }

    @Test
    void testConvertTxtToPdf_InvalidPdfCreation() throws Exception {
        // Arrange
        String txtContent = "This should throw an error during PDF creation.";
        when(txtFile.exists()).thenReturn(true);
        when(txtFile.isFile()).thenReturn(true);

        // Mock reading the content of the .txt file
        when(bufferedReader.readLine()).thenReturn(txtContent).thenReturn(null);

        // Simulate an error while creating the PDF
        PdfWriter writerMock = mock(PdfWriter.class);
        doThrow(new IOException("Error creating PDF")).when(writerMock).close();

        // Act & Assert
        assertThrows(IOException.class, () -> {
            txtToPdfService.convertTxtToPdf(txtFile, pdfFile);
        });
    }
}
