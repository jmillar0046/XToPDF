package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class StpToPdfServiceTest {

    private StpToPdfService stpToPdfService;
    private StepToPdfService stepToPdfService;
    private com.xtopdf.xtopdf.pdf.PdfBackendProvider pdfBackend;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        pdfBackend = new com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend();
        stepToPdfService = new StepToPdfService(pdfBackend);
        stpToPdfService = new StpToPdfService(stepToPdfService);
    }

    @Test
    void testConvertStpToPdf() throws Exception {
        String stepContent = "ISO-10303-21;\n" +
                             "HEADER;\n" +
                             "FILE_DESCRIPTION(('Test'),'2;1');\n" +
                             "FILE_NAME('test.stp','2024-01-01',('Author'),('Company'),'','','');\n" +
                             "FILE_SCHEMA(('AUTOMOTIVE_DESIGN'));\n" +
                             "ENDSEC;\n" +
                             "DATA;\n" +
                             "#1=CARTESIAN_POINT('',(0.0,0.0,0.0));\n" +
                             "#2=DIRECTION('',(1.0,0.0,0.0));\n" +
                             "ENDSEC;\n" +
                             "END-ISO-10303-21;";
        
        MockMultipartFile inputFile = new MockMultipartFile("file", "test.stp", "application/octet-stream", stepContent.getBytes());
        File outputFile = tempDir.resolve("output.pdf").toFile();

        stpToPdfService.convertStpToPdf(inputFile, outputFile);

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }
}
