package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RtfToPdfServiceTest {

    private RtfToPdfService rtfToPdfService;
    private File pdfFile;

    @BeforeEach
    void setUp() {
        rtfToPdfService = new RtfToPdfService();
    }

    @Test
    void testConvertRtfToPdf_Success() throws Exception {
        // Create a minimal RTF document
        String rtfContent = "{\\rtf1\\ansi\\deff0\n" +
                "{\\fonttbl{\\f0\\froman\\fcharset0 Times;}}\n" +
                "\\f0\\fs24 Hello World!\n" +
                "}";
        byte[] rtfData = rtfContent.getBytes();
        var rtfFile = new MockMultipartFile("file", "test.rtf", "application/rtf", rtfData);

        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/testRtfOutput.pdf");

        rtfToPdfService.convertRtfToPdf(rtfFile, pdfFile);

        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
    }

    @Test
    void testConvertRtfToPdf_NullMultipartFile_ThrowsIOException() {
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/nullInput.pdf");
        assertThrows(IOException.class, () -> rtfToPdfService.convertRtfToPdf(null, pdfFile));
    }

    @Test
    void testConvertRtfToPdf_NullOutputFile_ThrowsException() {
        String rtfContent = "{\\rtf1 Test}";
        var rtfFile = new MockMultipartFile("file", "test.rtf", "application/rtf", rtfContent.getBytes());
        assertThrows(Exception.class, () -> rtfToPdfService.convertRtfToPdf(rtfFile, null));
    }
}
