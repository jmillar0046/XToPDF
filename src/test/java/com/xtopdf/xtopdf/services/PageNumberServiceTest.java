package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.config.PageNumberConfig;
import com.xtopdf.xtopdf.enums.PageNumberAlignment;
import com.xtopdf.xtopdf.enums.PageNumberPosition;
import com.xtopdf.xtopdf.enums.PageNumberStyle;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
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

    @BeforeEach
    void setUp() {
        pageNumberService = new PageNumberService();
    }

    @Test
    void testAddPageNumbers_BottomCenter() throws Exception {
        File pdfFile = createTestPdf();
        
        PageNumberConfig config = new PageNumberConfig(
            true, PageNumberPosition.BOTTOM, PageNumberAlignment.CENTER, PageNumberStyle.ARABIC);
        
        pageNumberService.addPageNumbers(pdfFile, config);
        
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testAddPageNumbers_TopRight() throws Exception {
        File pdfFile = createTestPdf();
        
        PageNumberConfig config = new PageNumberConfig(
            true, PageNumberPosition.TOP, PageNumberAlignment.RIGHT, PageNumberStyle.ROMAN_UPPER);
        
        pageNumberService.addPageNumbers(pdfFile, config);
        
        assertTrue(pdfFile.exists());
    }

    @Test
    void testAddPageNumbers_BottomLeftAlphabetic() throws Exception {
        File pdfFile = createTestPdf();
        
        PageNumberConfig config = new PageNumberConfig(
            true, PageNumberPosition.BOTTOM, PageNumberAlignment.LEFT, PageNumberStyle.ALPHABETIC_LOWER);
        
        pageNumberService.addPageNumbers(pdfFile, config);
        
        assertTrue(pdfFile.exists());
    }

    @Test
    void testAddPageNumbers_DisabledConfig() throws Exception {
        File pdfFile = createTestPdf();
        long originalSize = pdfFile.length();
        
        PageNumberConfig config = PageNumberConfig.disabled();
        
        // Should not throw exception, but shouldn't modify file
        pageNumberService.addPageNumbers(pdfFile, config);
        
        // File should still exist
        assertTrue(pdfFile.exists());
    }

    @Test
    void testAddPageNumbers_MultiplePages() throws Exception {
        File pdfFile = createTestPdfWithMultiplePages(3);
        
        PageNumberConfig config = new PageNumberConfig(
            true, PageNumberPosition.BOTTOM, PageNumberAlignment.CENTER, PageNumberStyle.ARABIC);
        
        pageNumberService.addPageNumbers(pdfFile, config);
        
        assertTrue(pdfFile.exists());
        
        // Verify the PDF still has 3 pages
        try (PDDocument doc = org.apache.pdfbox.Loader.loadPDF(pdfFile)) {
            assertEquals(3, doc.getNumberOfPages());
        }
    }

    private File createTestPdf() throws IOException {
        File pdfFile = tempDir.resolve("test_page_numbers.pdf").toFile();
        
        try (PDDocument document = new PDDocument()) {
            document.addPage(new PDPage());
            document.save(pdfFile);
        }
        
        return pdfFile;
    }

    private File createTestPdfWithMultiplePages(int pageCount) throws IOException {
        File pdfFile = tempDir.resolve("test_page_numbers_multi.pdf").toFile();
        
        try (PDDocument document = new PDDocument()) {
            for (int i = 0; i < pageCount; i++) {
                document.addPage(new PDPage());
            }
            document.save(pdfFile);
        }
        
        return pdfFile;
    }
}
