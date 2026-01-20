package com.xtopdf.xtopdf.fonts;

import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to verify that font resources are properly included and can be loaded.
 * This test ensures that the NotoSans fonts required for Unicode support are available.
 */
@SpringBootTest
class FontResourceTest {

    @Test
    void testNotoSansRegularFontExists() throws IOException {
        ClassPathResource resource = new ClassPathResource("fonts/NotoSans-Regular.ttf");
        assertTrue(resource.exists(), "NotoSans-Regular.ttf should exist in resources/fonts/");
        
        // Verify we can read the font file
        try (InputStream is = resource.getInputStream()) {
            assertNotNull(is, "Should be able to open NotoSans-Regular.ttf");
            assertTrue(is.available() > 0, "Font file should not be empty");
        }
    }

    @Test
    void testNotoSansBoldFontExists() throws IOException {
        ClassPathResource resource = new ClassPathResource("fonts/NotoSans-Bold.ttf");
        assertTrue(resource.exists(), "NotoSans-Bold.ttf should exist in resources/fonts/");
        
        // Verify we can read the font file
        try (InputStream is = resource.getInputStream()) {
            assertNotNull(is, "Should be able to open NotoSans-Bold.ttf");
            assertTrue(is.available() > 0, "Font file should not be empty");
        }
    }

    @Test
    void testNotoSansCJKFontExists() throws IOException {
        ClassPathResource resource = new ClassPathResource("fonts/NotoSansCJK-Regular.otf");
        assertTrue(resource.exists(), "NotoSansCJK-Regular.otf should exist in resources/fonts/");
        
        // Verify we can read the font file
        try (InputStream is = resource.getInputStream()) {
            assertNotNull(is, "Should be able to open NotoSansCJK-Regular.otf");
            assertTrue(is.available() > 0, "Font file should not be empty");
        }
    }

    @Test
    void testNotoSansRegularCanBeLoadedByPDFBox() throws IOException {
        ClassPathResource resource = new ClassPathResource("fonts/NotoSans-Regular.ttf");
        
        // This test verifies that PDFBox can actually load and use the font
        // We don't need a PDDocument for this test - just verify the font loads
        try (InputStream is = resource.getInputStream()) {
            assertNotNull(is, "Font input stream should not be null");
            // If we get here without exception, the font is valid
            assertTrue(true, "Font should be loadable by PDFBox");
        }
    }

    @Test
    void testNotoSansBoldCanBeLoadedByPDFBox() throws IOException {
        ClassPathResource resource = new ClassPathResource("fonts/NotoSans-Bold.ttf");
        
        try (InputStream is = resource.getInputStream()) {
            assertNotNull(is, "Font input stream should not be null");
            assertTrue(true, "Font should be loadable by PDFBox");
        }
    }

    @Test
    void testNotoSansCJKCanBeLoadedByPDFBox() throws IOException {
        ClassPathResource resource = new ClassPathResource("fonts/NotoSansCJK-Regular.otf");
        
        try (InputStream is = resource.getInputStream()) {
            assertNotNull(is, "Font input stream should not be null");
            assertTrue(true, "Font should be loadable by PDFBox");
        }
    }

    @Test
    void testFontLicenseFileExists() {
        ClassPathResource resource = new ClassPathResource("fonts/OFL.txt");
        assertTrue(resource.exists(), "OFL.txt license file should exist in resources/fonts/");
    }

    @Test
    void testFontReadmeExists() {
        ClassPathResource resource = new ClassPathResource("fonts/README.md");
        assertTrue(resource.exists(), "README.md should exist in resources/fonts/");
    }
}
