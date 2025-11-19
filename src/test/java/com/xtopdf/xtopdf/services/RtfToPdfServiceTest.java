package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.*;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class RtfToPdfServiceTest {

    private RtfToPdfService rtfToPdfService;

    @BeforeEach
    void setUp() {
        rtfToPdfService = new RtfToPdfService();
    }

    @Test
    void testConvertRtfToPdf_SimpleDocument_Success(@TempDir Path tempDir) throws Exception {
        // Create a minimal RTF document
        String rtfContent = "{\\rtf1\\ansi\\deff0\n" +
                "{\\fonttbl{\\f0\\froman\\fcharset0 Times;}}\n" +
                "\\f0\\fs24 Hello World!\n" +
                "}";
        byte[] rtfData = rtfContent.getBytes();
        MockMultipartFile rtfFile = new MockMultipartFile(
                "file", 
                "test.rtf", 
                "application/rtf", 
                rtfData
        );

        File pdfFile = tempDir.resolve("testRtfOutput.pdf").toFile();

        rtfToPdfService.convertRtfToPdf(rtfFile, pdfFile);

        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
    }

    @Test
    void testConvertRtfToPdf_WithFormatting_Success(@TempDir Path tempDir) throws Exception {
        String rtfContent = "{\\rtf1\\ansi\\deff0\n" +
                "{\\fonttbl{\\f0\\froman\\fcharset0 Times;}}\n" +
                "\\f0\\fs24 \\b Bold Text\\b0 \\i Italic Text\\i0 \\ul Underline\\ul0\n" +
                "}";
        MockMultipartFile rtfFile = new MockMultipartFile(
                "file", 
                "formatted.rtf", 
                "application/rtf", 
                rtfContent.getBytes()
        );

        File pdfFile = tempDir.resolve("formatted_output.pdf").toFile();

        rtfToPdfService.convertRtfToPdf(rtfFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertRtfToPdf_MultipleParagraphs_Success(@TempDir Path tempDir) throws Exception {
        String rtfContent = "{\\rtf1\\ansi\\deff0\n" +
                "{\\fonttbl{\\f0\\froman\\fcharset0 Times;}}\n" +
                "\\f0\\fs24 Paragraph 1.\\par\n" +
                "Paragraph 2.\\par\n" +
                "Paragraph 3.\\par\n" +
                "}";
        MockMultipartFile rtfFile = new MockMultipartFile(
                "file", 
                "paragraphs.rtf", 
                "application/rtf", 
                rtfContent.getBytes()
        );

        File pdfFile = tempDir.resolve("paragraphs_output.pdf").toFile();

        rtfToPdfService.convertRtfToPdf(rtfFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertRtfToPdf_WithColors_Success(@TempDir Path tempDir) throws Exception {
        String rtfContent = "{\\rtf1\\ansi\\deff0\n" +
                "{\\fonttbl{\\f0\\froman\\fcharset0 Times;}}\n" +
                "{\\colortbl;\\red0\\green0\\blue0;\\red255\\green0\\blue0;\\red0\\green0\\blue255;}\n" +
                "\\f0\\fs24 \\cf1 Black \\cf2 Red \\cf3 Blue\\cf0\n" +
                "}";
        MockMultipartFile rtfFile = new MockMultipartFile(
                "file", 
                "colors.rtf", 
                "application/rtf", 
                rtfContent.getBytes()
        );

        File pdfFile = tempDir.resolve("colors_output.pdf").toFile();

        rtfToPdfService.convertRtfToPdf(rtfFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertRtfToPdf_WithLists_Success(@TempDir Path tempDir) throws Exception {
        String rtfContent = "{\\rtf1\\ansi\\deff0\n" +
                "{\\fonttbl{\\f0\\froman\\fcharset0 Times;}}\n" +
                "\\f0\\fs24 List items:\\par\n" +
                "\\bullet Item 1\\par\n" +
                "\\bullet Item 2\\par\n" +
                "\\bullet Item 3\\par\n" +
                "}";
        MockMultipartFile rtfFile = new MockMultipartFile(
                "file", 
                "lists.rtf", 
                "application/rtf", 
                rtfContent.getBytes()
        );

        File pdfFile = tempDir.resolve("lists_output.pdf").toFile();

        rtfToPdfService.convertRtfToPdf(rtfFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertRtfToPdf_LargeDocument_Success(@TempDir Path tempDir) throws Exception {
        StringBuilder rtfContent = new StringBuilder("{\\rtf1\\ansi\\deff0\n");
        rtfContent.append("{\\fonttbl{\\f0\\froman\\fcharset0 Times;}}\n");
        rtfContent.append("\\f0\\fs24\n");
        
        for (int i = 0; i < 100; i++) {
            rtfContent.append("This is line ").append(i).append(".\\par\n");
        }
        rtfContent.append("}");
        
        MockMultipartFile rtfFile = new MockMultipartFile(
                "file", 
                "large.rtf", 
                "application/rtf", 
                rtfContent.toString().getBytes()
        );

        File pdfFile = tempDir.resolve("large_output.pdf").toFile();

        rtfToPdfService.convertRtfToPdf(rtfFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertRtfToPdf_WithTables_Success(@TempDir Path tempDir) throws Exception {
        String rtfContent = "{\\rtf1\\ansi\\deff0\n" +
                "{\\fonttbl{\\f0\\froman\\fcharset0 Times;}}\n" +
                "\\f0\\fs24 Simple table:\\par\n" +
                "\\trowd\\cellx3000\\cellx6000\n" +
                "\\intbl Cell 1\\cell Cell 2\\cell\\row\n" +
                "\\intbl Cell 3\\cell Cell 4\\cell\\row\n" +
                "}";
        MockMultipartFile rtfFile = new MockMultipartFile(
                "file", 
                "table.rtf", 
                "application/rtf", 
                rtfContent.getBytes()
        );

        File pdfFile = tempDir.resolve("table_output.pdf").toFile();

        rtfToPdfService.convertRtfToPdf(rtfFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertRtfToPdf_WithSpecialCharacters_Success(@TempDir Path tempDir) throws Exception {
        String rtfContent = "{\\rtf1\\ansi\\deff0\n" +
                "{\\fonttbl{\\f0\\froman\\fcharset0 Times;}}\n" +
                "\\f0\\fs24 Special: \\'e9 \\'f1 \\'fc\\par\n" +
                "}";
        MockMultipartFile rtfFile = new MockMultipartFile(
                "file", 
                "special.rtf", 
                "application/rtf", 
                rtfContent.getBytes()
        );

        File pdfFile = tempDir.resolve("special_output.pdf").toFile();

        rtfToPdfService.convertRtfToPdf(rtfFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertRtfToPdf_NullMultipartFile_ThrowsIOException(@TempDir Path tempDir) {
        File pdfFile = tempDir.resolve("nullInput.pdf").toFile();
        assertThrows(IOException.class, 
            () -> rtfToPdfService.convertRtfToPdf(null, pdfFile));
    }

    @Test
    void testConvertRtfToPdf_NullOutputFile_ThrowsException() {
        String rtfContent = "{\\rtf1 Test}";
        MockMultipartFile rtfFile = new MockMultipartFile(
                "file", 
                "test.rtf", 
                "application/rtf", 
                rtfContent.getBytes()
        );
        assertThrows(Exception.class, 
            () -> rtfToPdfService.convertRtfToPdf(rtfFile, null));
    }

    @Test
    void testConvertRtfToPdf_EmptyDocument_Success(@TempDir Path tempDir) throws Exception {
        String rtfContent = "{\\rtf1\\ansi\\deff0\n" +
                "{\\fonttbl{\\f0\\froman\\fcharset0 Times;}}\n" +
                "\\f0\\fs24\n" +
                "}";
        MockMultipartFile rtfFile = new MockMultipartFile(
                "file", 
                "empty.rtf", 
                "application/rtf", 
                rtfContent.getBytes()
        );

        File pdfFile = tempDir.resolve("empty_output.pdf").toFile();

        rtfToPdfService.convertRtfToPdf(rtfFile, pdfFile);

        assertTrue(pdfFile.exists());
    }
}
