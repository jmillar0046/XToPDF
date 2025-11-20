package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PltToPdfServiceTest {

    private PltToPdfService pltToPdfService;
    
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        pltToPdfService = new PltToPdfService();
        pltToPdfService.pdfBackend = new PdfBoxBackend();
    }

    @Test
    void testConvertPltToPdf_ValidHpglFile() throws Exception {
        String content = "IN;SP1;PU0,0;PD100,0,100,100,0,100,0,0;PU;";
        
        MockMultipartFile pltFile = new MockMultipartFile(
                "file", "test.plt", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        File pdfFile = tempDir.resolve("testPltOutput.pdf").toFile();

        pltToPdfService.convertPltToPdf(pltFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertPltToPdf_WithLabels() throws Exception {
        String content = "IN;SP1;PA1000,1000;LBHello World\003;PU;";
        
        MockMultipartFile pltFile = new MockMultipartFile(
                "file", "test.plt", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        File pdfFile = tempDir.resolve("testPltLabelsOutput.pdf").toFile();

        pltToPdfService.convertPltToPdf(pltFile, pdfFile);

        assertTrue(pdfFile.exists());
    }

    @Test
    void testConvertPltToPdf_MultipleCommands() throws Exception {
        String content = "IN;SP2;PU100,200;PD300,400;AA500,600,45;AR700,800,-30;SP3;";
        
        MockMultipartFile pltFile = new MockMultipartFile(
                "file", "test.plt", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        File pdfFile = tempDir.resolve("testPltMultiOutput.pdf").toFile();

        pltToPdfService.convertPltToPdf(pltFile, pdfFile);

        assertTrue(pdfFile.exists());
    }

    @Test
    void testConvertPltToPdf_EmptyFile() throws Exception {
        MockMultipartFile pltFile = new MockMultipartFile(
                "file", "test.plt", MediaType.APPLICATION_OCTET_STREAM_VALUE, "".getBytes());

        File pdfFile = tempDir.resolve("testEmptyPltOutput.pdf").toFile();

        pltToPdfService.convertPltToPdf(pltFile, pdfFile);

        assertTrue(pdfFile.exists());
    }
}
