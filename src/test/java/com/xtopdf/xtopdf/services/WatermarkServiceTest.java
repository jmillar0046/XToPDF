package com.xtopdf.xtopdf.services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.xtopdf.xtopdf.config.WatermarkConfig;
import com.xtopdf.xtopdf.enums.WatermarkLayer;
import com.xtopdf.xtopdf.enums.WatermarkOrientation;
import org.junit.jupiter.api.AfterEach;
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
    
    private File testPdfFile;

    @BeforeEach
    void setUp() throws IOException {
        watermarkService = new WatermarkService();
        testPdfFile = tempDir.resolve("test.pdf").toFile();
        
        // Create a simple multi-page PDF for testing
        createTestPdf(testPdfFile, 3);
    }

    @AfterEach
    void tearDown() {
        if (testPdfFile != null && testPdfFile.exists()) {
            testPdfFile.delete();
        }
    }

    @Test
    void testAddWatermark_DisabledConfig_DoesNotModifyPdf() throws IOException {
        WatermarkConfig config = WatermarkConfig.disabled();
        long originalSize = testPdfFile.length();
        
        watermarkService.addWatermark(testPdfFile, config);
        
        // File should not be significantly modified
        assertTrue(testPdfFile.exists());
        // Size might change slightly due to PDF rewriting, but should be similar
        assertTrue(Math.abs(testPdfFile.length() - originalSize) < 1000);
    }

    @Test
    void testAddWatermark_NullText_DoesNotModifyPdf() throws IOException {
        WatermarkConfig config = WatermarkConfig.builder()
                .enabled(true)
                .text(null)
                .fontSize(48)
                .layer(WatermarkLayer.FOREGROUND)
                .orientation(WatermarkOrientation.HORIZONTAL)
                .build();
        
        long originalSize = testPdfFile.length();
        
        watermarkService.addWatermark(testPdfFile, config);
        
        assertTrue(testPdfFile.exists());
        assertTrue(Math.abs(testPdfFile.length() - originalSize) < 1000);
    }

    @Test
    void testAddWatermark_EmptyText_DoesNotModifyPdf() throws IOException {
        WatermarkConfig config = WatermarkConfig.builder()
                .enabled(true)
                .text("   ")
                .fontSize(48)
                .layer(WatermarkLayer.FOREGROUND)
                .orientation(WatermarkOrientation.HORIZONTAL)
                .build();
        
        long originalSize = testPdfFile.length();
        
        watermarkService.addWatermark(testPdfFile, config);
        
        assertTrue(testPdfFile.exists());
        assertTrue(Math.abs(testPdfFile.length() - originalSize) < 1000);
    }

    @Test
    void testAddWatermark_Horizontal_Foreground_Success() throws IOException {
        WatermarkConfig config = WatermarkConfig.builder()
                .enabled(true)
                .text("CONFIDENTIAL")
                .fontSize(48)
                .layer(WatermarkLayer.FOREGROUND)
                .orientation(WatermarkOrientation.HORIZONTAL)
                .build();
        
        watermarkService.addWatermark(testPdfFile, config);
        
        assertTrue(testPdfFile.exists());
        assertTrue(testPdfFile.length() > 0);
        
        // Verify the PDF can be read and has correct number of pages
        try (PdfDocument pdfDoc = new PdfDocument(new PdfReader(testPdfFile))) {
            assertEquals(3, pdfDoc.getNumberOfPages());
        }
    }

    @Test
    void testAddWatermark_Horizontal_Background_Success() throws IOException {
        WatermarkConfig config = WatermarkConfig.builder()
                .enabled(true)
                .text("DRAFT")
                .fontSize(60)
                .layer(WatermarkLayer.BACKGROUND)
                .orientation(WatermarkOrientation.HORIZONTAL)
                .build();
        
        watermarkService.addWatermark(testPdfFile, config);
        
        assertTrue(testPdfFile.exists());
        assertTrue(testPdfFile.length() > 0);
        
        try (PdfDocument pdfDoc = new PdfDocument(new PdfReader(testPdfFile))) {
            assertEquals(3, pdfDoc.getNumberOfPages());
        }
    }

    @Test
    void testAddWatermark_Vertical_Foreground_Success() throws IOException {
        WatermarkConfig config = WatermarkConfig.builder()
                .enabled(true)
                .text("SAMPLE")
                .fontSize(40)
                .layer(WatermarkLayer.FOREGROUND)
                .orientation(WatermarkOrientation.VERTICAL)
                .build();
        
        watermarkService.addWatermark(testPdfFile, config);
        
        assertTrue(testPdfFile.exists());
        assertTrue(testPdfFile.length() > 0);
        
        try (PdfDocument pdfDoc = new PdfDocument(new PdfReader(testPdfFile))) {
            assertEquals(3, pdfDoc.getNumberOfPages());
        }
    }

    @Test
    void testAddWatermark_Vertical_Background_Success() throws IOException {
        WatermarkConfig config = WatermarkConfig.builder()
                .enabled(true)
                .text("INTERNAL")
                .fontSize(50)
                .layer(WatermarkLayer.BACKGROUND)
                .orientation(WatermarkOrientation.VERTICAL)
                .build();
        
        watermarkService.addWatermark(testPdfFile, config);
        
        assertTrue(testPdfFile.exists());
        assertTrue(testPdfFile.length() > 0);
    }

    @Test
    void testAddWatermark_DiagonalUp_Foreground_Success() throws IOException {
        WatermarkConfig config = WatermarkConfig.builder()
                .enabled(true)
                .text("CONFIDENTIAL")
                .fontSize(55)
                .layer(WatermarkLayer.FOREGROUND)
                .orientation(WatermarkOrientation.DIAGONAL_UP)
                .build();
        
        watermarkService.addWatermark(testPdfFile, config);
        
        assertTrue(testPdfFile.exists());
        assertTrue(testPdfFile.length() > 0);
        
        try (PdfDocument pdfDoc = new PdfDocument(new PdfReader(testPdfFile))) {
            assertEquals(3, pdfDoc.getNumberOfPages());
        }
    }

    @Test
    void testAddWatermark_DiagonalUp_Background_Success() throws IOException {
        WatermarkConfig config = WatermarkConfig.builder()
                .enabled(true)
                .text("TOP SECRET")
                .fontSize(45)
                .layer(WatermarkLayer.BACKGROUND)
                .orientation(WatermarkOrientation.DIAGONAL_UP)
                .build();
        
        watermarkService.addWatermark(testPdfFile, config);
        
        assertTrue(testPdfFile.exists());
        assertTrue(testPdfFile.length() > 0);
    }

    @Test
    void testAddWatermark_DiagonalDown_Foreground_Success() throws IOException {
        WatermarkConfig config = WatermarkConfig.builder()
                .enabled(true)
                .text("WATERMARK")
                .fontSize(52)
                .layer(WatermarkLayer.FOREGROUND)
                .orientation(WatermarkOrientation.DIAGONAL_DOWN)
                .build();
        
        watermarkService.addWatermark(testPdfFile, config);
        
        assertTrue(testPdfFile.exists());
        assertTrue(testPdfFile.length() > 0);
        
        try (PdfDocument pdfDoc = new PdfDocument(new PdfReader(testPdfFile))) {
            assertEquals(3, pdfDoc.getNumberOfPages());
        }
    }

    @Test
    void testAddWatermark_DiagonalDown_Background_Success() throws IOException {
        WatermarkConfig config = WatermarkConfig.builder()
                .enabled(true)
                .text("RESTRICTED")
                .fontSize(48)
                .layer(WatermarkLayer.BACKGROUND)
                .orientation(WatermarkOrientation.DIAGONAL_DOWN)
                .build();
        
        watermarkService.addWatermark(testPdfFile, config);
        
        assertTrue(testPdfFile.exists());
        assertTrue(testPdfFile.length() > 0);
    }

    @Test
    void testAddWatermark_SmallFontSize_Success() throws IOException {
        WatermarkConfig config = WatermarkConfig.builder()
                .enabled(true)
                .text("Small Text")
                .fontSize(12)
                .layer(WatermarkLayer.FOREGROUND)
                .orientation(WatermarkOrientation.HORIZONTAL)
                .build();
        
        watermarkService.addWatermark(testPdfFile, config);
        
        assertTrue(testPdfFile.exists());
        assertTrue(testPdfFile.length() > 0);
    }

    @Test
    void testAddWatermark_LargeFontSize_Success() throws IOException {
        WatermarkConfig config = WatermarkConfig.builder()
                .enabled(true)
                .text("LARGE")
                .fontSize(100)
                .layer(WatermarkLayer.FOREGROUND)
                .orientation(WatermarkOrientation.HORIZONTAL)
                .build();
        
        watermarkService.addWatermark(testPdfFile, config);
        
        assertTrue(testPdfFile.exists());
        assertTrue(testPdfFile.length() > 0);
    }

    @Test
    void testAddWatermark_LongText_Success() throws IOException {
        WatermarkConfig config = WatermarkConfig.builder()
                .enabled(true)
                .text("This is a very long watermark text that should still work properly")
                .fontSize(24)
                .layer(WatermarkLayer.FOREGROUND)
                .orientation(WatermarkOrientation.HORIZONTAL)
                .build();
        
        watermarkService.addWatermark(testPdfFile, config);
        
        assertTrue(testPdfFile.exists());
        assertTrue(testPdfFile.length() > 0);
    }

    @Test
    void testAddWatermark_SinglePage_Success() throws IOException {
        File singlePagePdf = tempDir.resolve("single_page.pdf").toFile();
        createTestPdf(singlePagePdf, 1);
        
        WatermarkConfig config = WatermarkConfig.builder()
                .enabled(true)
                .text("WATERMARK")
                .fontSize(48)
                .layer(WatermarkLayer.FOREGROUND)
                .orientation(WatermarkOrientation.HORIZONTAL)
                .build();
        
        watermarkService.addWatermark(singlePagePdf, config);
        
        assertTrue(singlePagePdf.exists());
        try (PdfDocument pdfDoc = new PdfDocument(new PdfReader(singlePagePdf))) {
            assertEquals(1, pdfDoc.getNumberOfPages());
        }
    }

    @Test
    void testAddWatermark_ManyPages_Success() throws IOException {
        File manyPagesPdf = tempDir.resolve("many_pages.pdf").toFile();
        createTestPdf(manyPagesPdf, 10);
        
        WatermarkConfig config = WatermarkConfig.builder()
                .enabled(true)
                .text("CONFIDENTIAL")
                .fontSize(48)
                .layer(WatermarkLayer.FOREGROUND)
                .orientation(WatermarkOrientation.DIAGONAL_UP)
                .build();
        
        watermarkService.addWatermark(manyPagesPdf, config);
        
        assertTrue(manyPagesPdf.exists());
        try (PdfDocument pdfDoc = new PdfDocument(new PdfReader(manyPagesPdf))) {
            assertEquals(10, pdfDoc.getNumberOfPages());
        }
    }

    @Test
    void testAddWatermark_NonExistentFile_ThrowsException() {
        File nonExistentFile = tempDir.resolve("nonexistent.pdf").toFile();
        
        WatermarkConfig config = WatermarkConfig.builder()
                .enabled(true)
                .text("TEST")
                .fontSize(48)
                .layer(WatermarkLayer.FOREGROUND)
                .orientation(WatermarkOrientation.HORIZONTAL)
                .build();
        
        assertThrows(IOException.class, () -> {
            watermarkService.addWatermark(nonExistentFile, config);
        });
    }

    /**
     * Helper method to create a test PDF with multiple pages
     */
    private void createTestPdf(File file, int pageCount) throws IOException {
        try (PdfWriter writer = new PdfWriter(file);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document document = new Document(pdfDoc)) {
            
            for (int i = 1; i <= pageCount; i++) {
                document.add(new Paragraph("This is page " + i));
                if (i < pageCount) {
                    document.add(new com.itextpdf.layout.element.AreaBreak());
                }
            }
        }
    }
}
