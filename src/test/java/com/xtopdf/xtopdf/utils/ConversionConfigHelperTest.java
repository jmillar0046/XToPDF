package com.xtopdf.xtopdf.utils;

import com.xtopdf.xtopdf.config.PageNumberConfig;
import com.xtopdf.xtopdf.config.WatermarkConfig;
import com.xtopdf.xtopdf.dto.MergeRequest;
import com.xtopdf.xtopdf.dto.PageNumberRequest;
import com.xtopdf.xtopdf.dto.WatermarkRequest;
import com.xtopdf.xtopdf.enums.PageNumberAlignment;
import com.xtopdf.xtopdf.enums.PageNumberPosition;
import com.xtopdf.xtopdf.enums.PageNumberStyle;
import com.xtopdf.xtopdf.enums.WatermarkLayer;
import com.xtopdf.xtopdf.enums.WatermarkOrientation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConversionConfigHelperTest {

    @Test
    void testToPageNumberConfig_Null_ReturnsDisabled() {
        PageNumberConfig config = ConversionConfigHelper.toPageNumberConfig(null);
        assertNotNull(config);
        assertFalse(config.isEnabled());
    }

    @Test
    void testToPageNumberConfig_WithAllFields() {
        PageNumberRequest request = PageNumberRequest.builder()
                .position(PageNumberPosition.TOP)
                .alignment(PageNumberAlignment.LEFT)
                .style(PageNumberStyle.ROMAN_UPPER)
                .build();

        PageNumberConfig config = ConversionConfigHelper.toPageNumberConfig(request);

        assertTrue(config.isEnabled());
        assertEquals(PageNumberPosition.TOP, config.getPosition());
        assertEquals(PageNumberAlignment.LEFT, config.getAlignment());
        assertEquals(PageNumberStyle.ROMAN_UPPER, config.getStyle());
    }

    @Test
    void testToPageNumberConfig_WithDefaults() {
        PageNumberRequest request = PageNumberRequest.builder().build();

        PageNumberConfig config = ConversionConfigHelper.toPageNumberConfig(request);

        assertTrue(config.isEnabled());
        assertEquals(PageNumberPosition.BOTTOM, config.getPosition());
        assertEquals(PageNumberAlignment.CENTER, config.getAlignment());
        assertEquals(PageNumberStyle.ARABIC, config.getStyle());
    }

    @Test
    void testToWatermarkConfig_Null_ReturnsDisabled() {
        WatermarkConfig config = ConversionConfigHelper.toWatermarkConfig(null);
        assertNotNull(config);
        assertFalse(config.isEnabled());
    }

    @Test
    void testToWatermarkConfig_WithAllFields() {
        WatermarkRequest request = WatermarkRequest.builder()
                .text("CONFIDENTIAL")
                .fontSize(60f)
                .layer(WatermarkLayer.BACKGROUND)
                .orientation(WatermarkOrientation.HORIZONTAL)
                .build();

        WatermarkConfig config = ConversionConfigHelper.toWatermarkConfig(request);

        assertTrue(config.isEnabled());
        assertEquals("CONFIDENTIAL", config.getText());
        assertEquals(60f, config.getFontSize());
        assertEquals(WatermarkLayer.BACKGROUND, config.getLayer());
        assertEquals(WatermarkOrientation.HORIZONTAL, config.getOrientation());
    }

    @Test
    void testToWatermarkConfig_WithDefaults() {
        WatermarkRequest request = WatermarkRequest.builder()
                .text("TEST")
                .build();

        WatermarkConfig config = ConversionConfigHelper.toWatermarkConfig(request);

        assertTrue(config.isEnabled());
        assertEquals("TEST", config.getText());
        assertEquals(48f, config.getFontSize());
        assertEquals(WatermarkLayer.FOREGROUND, config.getLayer());
        assertEquals(WatermarkOrientation.DIAGONAL_UP, config.getOrientation());
    }

    @Test
    void testToWatermarkConfig_NullText_ThrowsException() {
        WatermarkRequest request = WatermarkRequest.builder()
                .text(null)
                .build();

        assertThrows(IllegalArgumentException.class, 
            () -> ConversionConfigHelper.toWatermarkConfig(request));
    }

    @Test
    void testToWatermarkConfig_EmptyText_ThrowsException() {
        WatermarkRequest request = WatermarkRequest.builder()
                .text("   ")
                .build();

        assertThrows(IllegalArgumentException.class, 
            () -> ConversionConfigHelper.toWatermarkConfig(request));
    }

    @Test
    void testToWatermarkConfig_FontSizeTooSmall_ThrowsException() {
        WatermarkRequest request = WatermarkRequest.builder()
                .text("TEST")
                .fontSize(0f)
                .build();

        assertThrows(IllegalArgumentException.class, 
            () -> ConversionConfigHelper.toWatermarkConfig(request));
    }

    @Test
    void testToWatermarkConfig_FontSizeTooLarge_ThrowsException() {
        WatermarkRequest request = WatermarkRequest.builder()
                .text("TEST")
                .fontSize(201f)
                .build();

        assertThrows(IllegalArgumentException.class, 
            () -> ConversionConfigHelper.toWatermarkConfig(request));
    }

    @Test
    void testExtractMergePosition_Null_ReturnsBack() {
        String position = ConversionConfigHelper.extractMergePosition(null);
        assertEquals("back", position);
    }

    @Test
    void testExtractMergePosition_Front() {
        MergeRequest request = MergeRequest.builder()
                .position("front")
                .build();

        String position = ConversionConfigHelper.extractMergePosition(request);
        assertEquals("front", position);
    }

    @Test
    void testExtractMergePosition_Back() {
        MergeRequest request = MergeRequest.builder()
                .position("back")
                .build();

        String position = ConversionConfigHelper.extractMergePosition(request);
        assertEquals("back", position);
    }

    @Test
    void testExtractMergePosition_CaseInsensitive() {
        MergeRequest request1 = MergeRequest.builder().position("FRONT").build();
        MergeRequest request2 = MergeRequest.builder().position("Back").build();

        assertEquals("FRONT", ConversionConfigHelper.extractMergePosition(request1));
        assertEquals("Back", ConversionConfigHelper.extractMergePosition(request2));
    }

    @Test
    void testExtractMergePosition_InvalidPosition_ThrowsException() {
        MergeRequest request = MergeRequest.builder()
                .position("middle")
                .build();

        assertThrows(IllegalArgumentException.class, 
            () -> ConversionConfigHelper.extractMergePosition(request));
    }

    @Test
    void testCreatePageNumberRequest() {
        PageNumberRequest request = ConversionConfigHelper.createPageNumberRequest(
                PageNumberPosition.TOP,
                PageNumberAlignment.RIGHT,
                PageNumberStyle.ROMAN_LOWER
        );

        assertNotNull(request);
        assertEquals(PageNumberPosition.TOP, request.getPosition());
        assertEquals(PageNumberAlignment.RIGHT, request.getAlignment());
        assertEquals(PageNumberStyle.ROMAN_LOWER, request.getStyle());
    }

    @Test
    void testCreateWatermarkRequest() {
        WatermarkRequest request = ConversionConfigHelper.createWatermarkRequest(
                "DRAFT",
                72f,
                WatermarkLayer.BACKGROUND,
                WatermarkOrientation.VERTICAL
        );

        assertNotNull(request);
        assertEquals("DRAFT", request.getText());
        assertEquals(72f, request.getFontSize());
        assertEquals(WatermarkLayer.BACKGROUND, request.getLayer());
        assertEquals(WatermarkOrientation.VERTICAL, request.getOrientation());
    }

    @Test
    void testCreateMergeRequest() {
        MergeRequest request = ConversionConfigHelper.createMergeRequest("front");

        assertNotNull(request);
        assertEquals("front", request.getPosition());
    }

    @Test
    void testCreateWatermarkRequest_WithNullValues() {
        WatermarkRequest request = ConversionConfigHelper.createWatermarkRequest(
                null, null, null, null
        );

        assertNotNull(request);
        assertNull(request.getText());
        assertNull(request.getFontSize());
        assertNull(request.getLayer());
        assertNull(request.getOrientation());
    }
}
