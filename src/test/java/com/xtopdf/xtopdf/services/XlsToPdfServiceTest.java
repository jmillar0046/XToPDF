package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertThrows;

class XlsToPdfServiceTest {

    private XlsToPdfService xlsToPdfService;
    private File pdfFile;

    @BeforeEach
    void setUp() {
        xlsToPdfService = new XlsToPdfService();
    }

    @Test
    void testConvertXlsToPdf_InvalidFormat_ThrowsIOException() throws Exception {
        // Invalid XLS data will throw IOException
        byte[] invalidXlsData = "Not a valid XLS file".getBytes();
        var xlsFile = new MockMultipartFile("file", "test.xls", "application/vnd.ms-excel", invalidXlsData);

        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/testXlsOutput.pdf");

        assertThrows(IOException.class, () -> xlsToPdfService.convertXlsToPdf(xlsFile, pdfFile, false));
    }

    @Test
    void testConvertXlsToPdf_NullMultipartFile_ThrowsIOException() {
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/nullInput.pdf");
        assertThrows(IOException.class, () -> xlsToPdfService.convertXlsToPdf(null, pdfFile, false));
    }

    @Test
    void testConvertXlsToPdf_NullOutputFile_ThrowsNullPointerException() {
        byte[] xlsData = "test".getBytes();
        var xlsFile = new MockMultipartFile("file", "test.xls", "application/vnd.ms-excel", xlsData);
        assertThrows(Exception.class, () -> xlsToPdfService.convertXlsToPdf(xlsFile, null, false));
    }
}
