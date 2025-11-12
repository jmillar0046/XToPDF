package com.xtopdf.xtopdf.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration for merging with an existing PDF.
 * 
 * When using this in ConversionRequest, you must also provide the 'existingPdf' 
 * multipart file parameter containing the PDF to merge with.
 * 
 * Example usage:
 * <pre>
 * curl -X POST http://localhost:8080/api/convert/json \
 *   -F "inputFile=@document.docx" \
 *   -F "existingPdf=@existing.pdf" \
 *   -F 'request={"outputFile":"output.pdf","merge":{"position":"back"}}'
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MergeRequest {
    /**
     * Position where the existing PDF should be placed relative to the converted content.
     * - "front": Place existing PDF before the converted content (prepend)
     * - "back": Place existing PDF after the converted content (append, default)
     */
    private String position; // "front" or "back"
}
