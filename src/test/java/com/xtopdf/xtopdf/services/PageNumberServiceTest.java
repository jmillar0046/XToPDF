package com.xtopdf.xtopdf.services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.xtopdf.xtopdf.config.PageNumberConfig;
import com.xtopdf.xtopdf.enums.PageNumberAlignment;
import com.xtopdf.xtopdf.enums.PageNumberPosition;
import com.xtopdf.xtopdf.enums.PageNumberStyle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PageNumberServiceTest {

    private PageNumberService pageNumberService;
    
    @TempDir
    Path tempDir;
    
    private File testPdfFile;

    @BeforeEach
    void setUp() throws IOException {
        pageNumberService = new PageNumberService();
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
    void testAddPageNumbers_DisabledConfig_DoesNotModifyPdf() throws IOException {
        PageNumberConfig config = PageNumberConfig.disabled();
        long originalSize = testPdfFile.length();
        
        pageNumberService.addPageNumbers(testPdfFile, config);
        
        // File should not be significantly modified
        assertTrue(testPdfFile.exists());
        // Size might change slightly due to PDF rewriting, but should be similar
        assertTrue(Math.abs(testPdfFile.length() - originalSize) < 1000);
    }

    @Test
    void testAddPageNumbers_ArabicNumbers_Success() throws IOException {
        PageNumberConfig config = PageNumberConfig.builder()
                .enabled(true)
                .position(PageNumberPosition.BOTTOM)
                .alignment(PageNumberAlignment.CENTER)
                .style(PageNumberStyle.ARABIC)
                .build();
        
        pageNumberService.addPageNumbers(testPdfFile, config);
        
        assertTrue(testPdfFile.exists());
        assertTrue(testPdfFile.length() > 0);
        
        // Verify the PDF can be read and has correct number of pages
        try (PdfDocument pdfDoc = new PdfDocument(new PdfReader(testPdfFile))) {
            assertEquals(3, pdfDoc.getNumberOfPages());
        }
    }

    @Test
    void testAddPageNumbers_RomanUpperNumbers_Success() throws IOException {
        PageNumberConfig config = PageNumberConfig.builder()
                .enabled(true)
                .position(PageNumberPosition.TOP)
                .alignment(PageNumberAlignment.RIGHT)
                .style(PageNumberStyle.ROMAN_UPPER)
                .build();
        
        pageNumberService.addPageNumbers(testPdfFile, config);
        
        assertTrue(testPdfFile.exists());
        assertTrue(testPdfFile.length() > 0);
        
        try (PdfDocument pdfDoc = new PdfDocument(new PdfReader(testPdfFile))) {
            assertEquals(3, pdfDoc.getNumberOfPages());
        }
    }

    @Test
    void testAddPageNumbers_RomanLowerNumbers_Success() throws IOException {
        PageNumberConfig config = PageNumberConfig.builder()
                .enabled(true)
                .position(PageNumberPosition.BOTTOM)
                .alignment(PageNumberAlignment.LEFT)
                .style(PageNumberStyle.ROMAN_LOWER)
                .build();
        
        pageNumberService.addPageNumbers(testPdfFile, config);
        
        assertTrue(testPdfFile.exists());
        assertTrue(testPdfFile.length() > 0);
    }

    @Test
    void testAddPageNumbers_AlphabeticUpperNumbers_Success() throws IOException {
        PageNumberConfig config = PageNumberConfig.builder()
                .enabled(true)
                .position(PageNumberPosition.TOP)
                .alignment(PageNumberAlignment.CENTER)
                .style(PageNumberStyle.ALPHABETIC_UPPER)
                .build();
        
        pageNumberService.addPageNumbers(testPdfFile, config);
        
        assertTrue(testPdfFile.exists());
        assertTrue(testPdfFile.length() > 0);
    }

    @Test
    void testAddPageNumbers_AlphabeticLowerNumbers_Success() throws IOException {
        PageNumberConfig config = PageNumberConfig.builder()
                .enabled(true)
                .position(PageNumberPosition.BOTTOM)
                .alignment(PageNumberAlignment.RIGHT)
                .style(PageNumberStyle.ALPHABETIC_LOWER)
                .build();
        
        pageNumberService.addPageNumbers(testPdfFile, config);
        
        assertTrue(testPdfFile.exists());
        assertTrue(testPdfFile.length() > 0);
    }

    @Test
    void testAddPageNumbers_TopPosition_Success() throws IOException {
        PageNumberConfig config = PageNumberConfig.builder()
                .enabled(true)
                .position(PageNumberPosition.TOP)
                .alignment(PageNumberAlignment.CENTER)
                .style(PageNumberStyle.ARABIC)
                .build();
        
        pageNumberService.addPageNumbers(testPdfFile, config);
        
        assertTrue(testPdfFile.exists());
    }

    @Test
    void testAddPageNumbers_BottomPosition_Success() throws IOException {
        PageNumberConfig config = PageNumberConfig.builder()
                .enabled(true)
                .position(PageNumberPosition.BOTTOM)
                .alignment(PageNumberAlignment.CENTER)
                .style(PageNumberStyle.ARABIC)
                .build();
        
        pageNumberService.addPageNumbers(testPdfFile, config);
        
        assertTrue(testPdfFile.exists());
    }

    @Test
    void testAddPageNumbers_LeftAlignment_Success() throws IOException {
        PageNumberConfig config = PageNumberConfig.builder()
                .enabled(true)
                .position(PageNumberPosition.BOTTOM)
                .alignment(PageNumberAlignment.LEFT)
                .style(PageNumberStyle.ARABIC)
                .build();
        
        pageNumberService.addPageNumbers(testPdfFile, config);
        
        assertTrue(testPdfFile.exists());
    }

    @Test
    void testAddPageNumbers_CenterAlignment_Success() throws IOException {
        PageNumberConfig config = PageNumberConfig.builder()
                .enabled(true)
                .position(PageNumberPosition.BOTTOM)
                .alignment(PageNumberAlignment.CENTER)
                .style(PageNumberStyle.ARABIC)
                .build();
        
        pageNumberService.addPageNumbers(testPdfFile, config);
        
        assertTrue(testPdfFile.exists());
    }

    @Test
    void testAddPageNumbers_RightAlignment_Success() throws IOException {
        PageNumberConfig config = PageNumberConfig.builder()
                .enabled(true)
                .position(PageNumberPosition.BOTTOM)
                .alignment(PageNumberAlignment.RIGHT)
                .style(PageNumberStyle.ARABIC)
                .build();
        
        pageNumberService.addPageNumbers(testPdfFile, config);
        
        assertTrue(testPdfFile.exists());
    }

    @Test
    void testAddPageNumbers_SinglePage_Success() throws IOException {
        File singlePagePdf = tempDir.resolve("single_page.pdf").toFile();
        createTestPdf(singlePagePdf, 1);
        
        PageNumberConfig config = PageNumberConfig.builder()
                .enabled(true)
                .position(PageNumberPosition.BOTTOM)
                .alignment(PageNumberAlignment.CENTER)
                .style(PageNumberStyle.ARABIC)
                .build();
        
        pageNumberService.addPageNumbers(singlePagePdf, config);
        
        assertTrue(singlePagePdf.exists());
        try (PdfDocument pdfDoc = new PdfDocument(new PdfReader(singlePagePdf))) {
            assertEquals(1, pdfDoc.getNumberOfPages());
        }
    }

    @Test
    void testAddPageNumbers_ManyPages_Success() throws IOException {
        File manyPagesPdf = tempDir.resolve("many_pages.pdf").toFile();
        createTestPdf(manyPagesPdf, 10);
        
        PageNumberConfig config = PageNumberConfig.builder()
                .enabled(true)
                .position(PageNumberPosition.BOTTOM)
                .alignment(PageNumberAlignment.CENTER)
                .style(PageNumberStyle.ARABIC)
                .build();
        
        pageNumberService.addPageNumbers(manyPagesPdf, config);
        
        assertTrue(manyPagesPdf.exists());
        try (PdfDocument pdfDoc = new PdfDocument(new PdfReader(manyPagesPdf))) {
            assertEquals(10, pdfDoc.getNumberOfPages());
        }
    }

    @Test
    void testAddPageNumbers_NonExistentFile_ThrowsException() {
        File nonExistentFile = tempDir.resolve("nonexistent.pdf").toFile();
        
        PageNumberConfig config = PageNumberConfig.builder()
                .enabled(true)
                .position(PageNumberPosition.BOTTOM)
                .alignment(PageNumberAlignment.CENTER)
                .style(PageNumberStyle.ARABIC)
                .build();
        
        assertThrows(IOException.class, () -> {
            pageNumberService.addPageNumbers(nonExistentFile, config);
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
