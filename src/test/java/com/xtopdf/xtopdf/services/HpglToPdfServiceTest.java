package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HpglToPdfServiceTest {

    private HpglToPdfService hpglToPdfService;
    private PltToPdfService pltToPdfService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        pltToPdfService = new PltToPdfService();
        hpglToPdfService = new HpglToPdfService(pltToPdfService);
    }

    @Test
    void testConvertHpglToPdf() throws Exception {
        String hpglContent = "IN;SP1;PU0,0;PD100,100;PU200,200;PD300,300;";
        
        MockMultipartFile inputFile = new MockMultipartFile("file", "test.hpgl", "application/octet-stream", hpglContent.getBytes());
        File outputFile = tempDir.resolve("output.pdf").toFile();

        hpglToPdfService.convertHpglToPdf(inputFile, outputFile);

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }
}
