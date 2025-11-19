package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.config.WatermarkConfig;
import com.xtopdf.xtopdf.enums.WatermarkLayer;
import com.xtopdf.xtopdf.enums.WatermarkOrientation;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class WatermarkServiceTest {

    private WatermarkService watermarkService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        watermarkService = new WatermarkService();
    }

    @Test
    void testAddWatermark_DiagonalUp() throws Exception {
        File pdfFile = createTestPdf();
        
        WatermarkConfig config = new WatermarkConfig(
            true, "CONFIDENTIAL", 48, WatermarkLayer.FOREGROUND, WatermarkOrientation.DIAGONAL_UP);
        
        watermarkService.addWatermark(pdfFile, config);
        
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testAddWatermark_Horizontal() throws Exception {
        File pdfFile = createTestPdf();
        
        WatermarkConfig config = new WatermarkConfig(
            true, "DRAFT", 36, WatermarkLayer.BACKGROUND, WatermarkOrientation.HORIZONTAL);
        
        watermarkService.addWatermark(pdfFile, config);
        
        assertTrue(pdfFile.exists());
    }

    @Test
    void testAddWatermark_Vertical() throws Exception {
        File pdfFile = createTestPdf();
        
        WatermarkConfig config = new WatermarkConfig(
            true, "SAMPLE", 40, WatermarkLayer.FOREGROUND, WatermarkOrientation.VERTICAL);
        
        watermarkService.addWatermark(pdfFile, config);
        
        assertTrue(pdfFile.exists());
    }

    @Test
    void testAddWatermark_DiagonalDown() throws Exception {
        File pdfFile = createTestPdf();
        
        WatermarkConfig config = new WatermarkConfig(
            true, "TEST", 50, WatermarkLayer.BACKGROUND, WatermarkOrientation.DIAGONAL_DOWN);
        
        watermarkService.addWatermark(pdfFile, config);
        
        assertTrue(pdfFile.exists());
    }

    @Test
    void testAddWatermark_DisabledConfig() throws Exception {
        File pdfFile = createTestPdf();
        
        WatermarkConfig config = WatermarkConfig.disabled();
        
        // Should not throw exception
        watermarkService.addWatermark(pdfFile, config);
        
        assertTrue(pdfFile.exists());
    }

    private File createTestPdf() throws IOException {
        File pdfFile = tempDir.resolve("test_watermark.pdf").toFile();
        
        try (PDDocument document = new PDDocument()) {
            document.addPage(new PDPage());
            document.save(pdfFile);
        }
        
        return pdfFile;
    }
}
