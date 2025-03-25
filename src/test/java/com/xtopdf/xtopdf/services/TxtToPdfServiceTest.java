package com.xtopdf.xtopdf.services;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class TxtToPdfServiceTest {

    private TxtToPdfService txtToPdfService;

    private File txtFile;

    private File pdfFile;

    @BeforeEach
    void setUp() {
        txtToPdfService = new TxtToPdfService();
    }


    @Test
    void testConvertTxtToPdf_Success() throws Exception {
        txtFile = new File("src/test/resources/test.txt");
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/testOutput.pdf");

        txtToPdfService.convertTxtToPdf(txtFile, pdfFile);

        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
    }

    @Test
    void testConvertTxtToPdf_FileNotFound() {
        txtFile = new File("src/test/resources/badPath.txt");
        assertThrows(IOException.class, () -> {
            txtToPdfService.convertTxtToPdf(txtFile, pdfFile);
        });
    }

    @Test
    void testConvertTxtToPdf_EmptyFile() throws Exception {
        txtFile = new File("src/test/resources/empty.txt");
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/testEmptyOutput.pdf");

        txtToPdfService.convertTxtToPdf(txtFile, pdfFile);

        assertTrue(pdfFile.exists(), "The PDF file should be created.");
    }

    @Test
    void testConvertTxtToPdf_InvalidPdfCreation() throws Exception {
        txtFile = new File("src/test/resources/empty.txt");

        assertThrows(IOException.class, () -> {
            txtToPdfService.convertTxtToPdf(txtFile, pdfFile);
        });
    }
}
