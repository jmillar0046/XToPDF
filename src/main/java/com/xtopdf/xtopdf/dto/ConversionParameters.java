package com.xtopdf.xtopdf.dto;

import com.xtopdf.xtopdf.config.PageNumberConfig;
import com.xtopdf.xtopdf.config.WatermarkConfig;
import org.springframework.web.multipart.MultipartFile;

/**
 * Immutable parameter object for file conversion requests.
 * Replaces the multiple overloaded convertFile() methods on FileConversionService
 * with a single method accepting this record.
 */
public record ConversionParameters(
        MultipartFile inputFile,
        String outputFile,
        MultipartFile existingPdf,
        String position,
        PageNumberConfig pageNumberConfig,
        WatermarkConfig watermarkConfig,
        boolean executeMacros
) {
    public ConversionParameters {
        if (pageNumberConfig == null) pageNumberConfig = PageNumberConfig.disabled();
        if (watermarkConfig == null) watermarkConfig = WatermarkConfig.disabled();
    }

    /**
     * Factory method for simple conversions with sensible defaults.
     */
    public static ConversionParameters of(MultipartFile inputFile, String outputFile) {
        return new ConversionParameters(inputFile, outputFile, null, null,
                PageNumberConfig.disabled(), WatermarkConfig.disabled(), false);
    }
}
