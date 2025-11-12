package com.xtopdf.xtopdf.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request object for file conversion with optional PDF operations.
 * 
 * This is used with the /api/convert/json endpoint which accepts:
 * - inputFile: The file to convert (multipart parameter)
 * - existingPdf: (Optional) PDF to merge with (multipart parameter, required if merge is specified)
 * - request: This JSON configuration object (multipart parameter)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversionRequest {
    /** Output PDF file name (required) */
    private String outputFile;
    
    /** Whether to execute macros/recalculate formulas for Excel files */
    private Boolean executeMacros;
    
    /** Optional page numbering configuration */
    private PageNumberRequest pageNumbers;
    
    /** Optional watermark configuration */
    private WatermarkRequest watermark;
    
    /** 
     * Optional merge configuration. 
     * If specified, you must also provide the 'existingPdf' multipart parameter.
     */
    private MergeRequest merge;
}
