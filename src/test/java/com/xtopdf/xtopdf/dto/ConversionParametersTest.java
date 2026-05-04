package com.xtopdf.xtopdf.dto;

import com.xtopdf.xtopdf.config.PageNumberConfig;
import com.xtopdf.xtopdf.config.WatermarkConfig;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Tests for ConversionParameters record.
 * Validates: Requirements 6.2, 6.3
 */
class ConversionParametersTest {

    @Test
    void of_setsCorrectDefaults() {
        MultipartFile inputFile = mock(MultipartFile.class);
        String outputFile = "output.pdf";

        ConversionParameters params = ConversionParameters.of(inputFile, outputFile);

        assertSame(inputFile, params.inputFile());
        assertEquals(outputFile, params.outputFile());
        assertNull(params.existingPdf());
        assertNull(params.position());
        assertFalse(params.pageNumberConfig().isEnabled());
        assertFalse(params.watermarkConfig().isEnabled());
        assertFalse(params.executeMacros());
    }

    @Test
    void compactConstructor_setsPageNumberConfigToDisabledWhenNull() {
        MultipartFile inputFile = mock(MultipartFile.class);

        ConversionParameters params = new ConversionParameters(
                inputFile, "output.pdf", null, null,
                null, WatermarkConfig.disabled(), false
        );

        assertNotNull(params.pageNumberConfig());
        assertFalse(params.pageNumberConfig().isEnabled());
    }

    @Test
    void compactConstructor_setsWatermarkConfigToDisabledWhenNull() {
        MultipartFile inputFile = mock(MultipartFile.class);

        ConversionParameters params = new ConversionParameters(
                inputFile, "output.pdf", null, null,
                PageNumberConfig.disabled(), null, false
        );

        assertNotNull(params.watermarkConfig());
        assertFalse(params.watermarkConfig().isEnabled());
    }

    @Test
    void allFieldsAreAccessible() {
        MultipartFile inputFile = mock(MultipartFile.class);
        MultipartFile existingPdf = mock(MultipartFile.class);
        PageNumberConfig pageNumberConfig = PageNumberConfig.disabled();
        WatermarkConfig watermarkConfig = WatermarkConfig.disabled();

        ConversionParameters params = new ConversionParameters(
                inputFile, "output.pdf", existingPdf, "front",
                pageNumberConfig, watermarkConfig, true
        );

        assertSame(inputFile, params.inputFile());
        assertEquals("output.pdf", params.outputFile());
        assertSame(existingPdf, params.existingPdf());
        assertEquals("front", params.position());
        assertSame(pageNumberConfig, params.pageNumberConfig());
        assertSame(watermarkConfig, params.watermarkConfig());
        assertTrue(params.executeMacros());
    }
}
