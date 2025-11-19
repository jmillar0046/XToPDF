package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class IgsToPdfServiceTest {

    private IgsToPdfService igsToPdfService;
    private IgesToPdfService igesToPdfService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        igesToPdfService = new IgesToPdfService();
        igsToPdfService = new IgsToPdfService(igesToPdfService);
    }

    @Test
    void testConvertIgsToPdf() throws Exception {
        String igesContent = "                                                                        S      1\n" +
                             "1H,,1H;,7Htest.igs,14HTest IGES File,                                  G      1\n" +
                             "12HVersion 5.3,32,308,15,308,15,7Htest.igs,1.,1,4HINCH,1,0.001,        G      2\n" +
                             "15H20240101.120000,0.001,10000.,7HAuthor,10HCompany,11,0,             G      3\n" +
                             "15H20240101.120000;                                                    G      4\n" +
                             "     116       1       0       0       0       0       0       000000001D      1\n" +
                             "     116       0       0       1       0                               D      2\n" +
                             "116,0.,0.,0.;                                                          1P      1\n" +
                             "S      1G      4D      2P      1                                        T      1";
        
        MockMultipartFile inputFile = new MockMultipartFile("file", "test.igs", "application/octet-stream", igesContent.getBytes());
        File outputFile = tempDir.resolve("output.pdf").toFile();

        igsToPdfService.convertIgsToPdf(inputFile, outputFile);

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }
}
