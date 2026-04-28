package com.xtopdf.xtopdf.services.conversion.document;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for color parsing in DocxToPdfService.
 * These tests are in the same package as DocxToPdfService to access
 * the package-private parseColor() method.
 *
 * Validates: Requirements 3.1, 3.2, 3.5
 */
class DocxToPdfServiceColorTest {

    private DocxToPdfService docxToPdfService;

    @BeforeEach
    void setUp() {
        var pdfBackend = new com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend();
        docxToPdfService = new DocxToPdfService(pdfBackend);
    }

    @Test
    void testParseColorValidRedHex() {
        int[] rgb = docxToPdfService.parseColor("FF0000");
        assertEquals(255, rgb[0], "Red component should be 255");
        assertEquals(0, rgb[1], "Green component should be 0");
        assertEquals(0, rgb[2], "Blue component should be 0");
    }

    @Test
    void testParseColorValidGreenHex() {
        int[] rgb = docxToPdfService.parseColor("00FF00");
        assertEquals(0, rgb[0], "Red component should be 0");
        assertEquals(255, rgb[1], "Green component should be 255");
        assertEquals(0, rgb[2], "Blue component should be 0");
    }

    @Test
    void testParseColorNullReturnsBlack() {
        int[] rgb = docxToPdfService.parseColor(null);
        assertEquals(0, rgb[0], "Red component should be 0 for null");
        assertEquals(0, rgb[1], "Green component should be 0 for null");
        assertEquals(0, rgb[2], "Blue component should be 0 for null");
    }

    @Test
    void testParseColorInvalidHexReturnsBlack() {
        int[] rgb = docxToPdfService.parseColor("ZZZZZZ");
        assertEquals(0, rgb[0], "Red component should be 0 for invalid hex");
        assertEquals(0, rgb[1], "Green component should be 0 for invalid hex");
        assertEquals(0, rgb[2], "Blue component should be 0 for invalid hex");
    }

    @Test
    void testParseColorTooShortReturnsBlack() {
        int[] rgb = docxToPdfService.parseColor("FFF");
        assertEquals(0, rgb[0], "Red component should be 0 for short string");
        assertEquals(0, rgb[1], "Green component should be 0 for short string");
        assertEquals(0, rgb[2], "Blue component should be 0 for short string");
    }
}
