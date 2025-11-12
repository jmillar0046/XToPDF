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

/**
 * Helper utility for converting between request DTOs and configuration objects
 */
public class ConversionConfigHelper {

    /**
     * Converts PageNumberRequest DTO to PageNumberConfig
     */
    public static PageNumberConfig toPageNumberConfig(PageNumberRequest request) {
        if (request == null) {
            return PageNumberConfig.disabled();
        }
        
        return PageNumberConfig.builder()
                .enabled(true)
                .position(request.getPosition() != null ? request.getPosition() : PageNumberPosition.BOTTOM)
                .alignment(request.getAlignment() != null ? request.getAlignment() : PageNumberAlignment.CENTER)
                .style(request.getStyle() != null ? request.getStyle() : PageNumberStyle.ARABIC)
                .build();
    }

    /**
     * Converts watermark request DTO to WatermarkConfig
     */
    public static WatermarkConfig toWatermarkConfig(WatermarkRequest request) {
        if (request == null) {
            return WatermarkConfig.disabled();
        }
        
        String text = request.getText();
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Watermark text must be provided when addWatermark is true");
        }
        
        float fontSize = request.getFontSize() != null ? request.getFontSize() : 48f;
        if (fontSize <= 0 || fontSize > 200) {
            throw new IllegalArgumentException("Watermark font size must be greater than 0 and up to 200");
        }
        
        return WatermarkConfig.builder()
                .enabled(true)
                .text(text)
                .fontSize(fontSize)
                .layer(request.getLayer() != null ? request.getLayer() : WatermarkLayer.FOREGROUND)
                .orientation(request.getOrientation() != null ? request.getOrientation() : WatermarkOrientation.DIAGONAL_UP)
                .build();
    }

    /**
     * Validates and extracts position from MergeRequest
     */
    public static String extractMergePosition(MergeRequest request) {
        if (request == null || request.getPosition() == null) {
            return "back";
        }
        
        String position = request.getPosition();
        if (!position.equalsIgnoreCase("front") && !position.equalsIgnoreCase("back")) {
            throw new IllegalArgumentException("Invalid position. Must be 'front' or 'back'");
        }
        
        return position;
    }

    /**
     * Creates PageNumberRequest from individual parameters
     */
    public static PageNumberRequest createPageNumberRequest(
            PageNumberPosition position,
            PageNumberAlignment alignment,
            PageNumberStyle style) {
        return PageNumberRequest.builder()
                .position(position)
                .alignment(alignment)
                .style(style)
                .build();
    }

    /**
     * Creates WatermarkRequest from individual parameters
     */
    public static WatermarkRequest createWatermarkRequest(
            String text,
            Float fontSize,
            WatermarkLayer layer,
            WatermarkOrientation orientation) {
        return WatermarkRequest.builder()
                .text(text)
                .fontSize(fontSize)
                .layer(layer)
                .orientation(orientation)
                .build();
    }

    /**
     * Creates MergeRequest from position string
     */
    public static MergeRequest createMergeRequest(String position) {
        return MergeRequest.builder()
                .position(position)
                .build();
    }
}
