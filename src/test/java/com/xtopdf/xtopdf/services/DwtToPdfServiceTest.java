package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DwtToPdfServiceTest {

    private DwtToPdfService dwtToPdfService;
    private DwgToPdfService dwgToPdfService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        DwgToDxfService dwgToDxfService = new DwgToDxfService();
        DxfToPdfService dxfToPdfService = new DxfToPdfService();
        dwgToPdfService = new DwgToPdfService(dwgToDxfService, dxfToPdfService);
        dwtToPdfService = new DwtToPdfService(dwgToPdfService);
    }

    @Test
    void testConvertDwtToPdf() throws Exception {
        // Create simple binary DWT file (same as DWG format)
        byte[] dwtContent = new byte[]{
            1, // LINE type
            0, 0, 0, 0, 0, 0, 0, 0, // x1 = 0.0
            0, 0, 0, 0, 0, 0, 0, 0, // y1 = 0.0
            64, 89, 0, 0, 0, 0, 0, 0, // x2 = 100.0
            64, 89, 0, 0, 0, 0, 0, 0  // y2 = 100.0
        };
        
        MockMultipartFile inputFile = new MockMultipartFile("file", "test.dwt", "application/octet-stream", dwtContent);
        File outputFile = tempDir.resolve("output.pdf").toFile();

        dwtToPdfService.convertDwtToPdf(inputFile, outputFile);

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }
}
