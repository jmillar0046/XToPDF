package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class StepToPdfServiceTest {

    private StepToPdfService stepToPdfService;
    private com.xtopdf.xtopdf.pdf.PdfBackendProvider pdfBackend;
    
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        pdfBackend = new com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend();
        stepToPdfService = new StepToPdfService(pdfBackend);
    }

    @Test
    void testConvertStepToPdf_Success() throws Exception {
        String content = "ISO-10303-21;\nHEADER;\nENDSEC;\nDATA;\n#1=CARTESIAN_POINT('',(0.,0.,0.));\nENDSEC;\nEND-ISO-10303-21;";
        
        MockMultipartFile stepFile = new MockMultipartFile(
                "file", "test.step", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        File pdfFile = tempDir.resolve("testStepOutput.pdf").toFile();

        stepToPdfService.convertStepToPdf(stepFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertStepToPdf_EmptyFile() throws Exception {
        MockMultipartFile stepFile = new MockMultipartFile(
                "file", "test.step", MediaType.APPLICATION_OCTET_STREAM_VALUE, "".getBytes());

        File pdfFile = tempDir.resolve("testEmptyStepOutput.pdf").toFile();

        stepToPdfService.convertStepToPdf(stepFile, pdfFile);

        assertTrue(pdfFile.exists());
    }
}
