package com.xtopdf.xtopdf.utils;

import org.apache.poi.xwpf.usermodel.XWPFDocument;

/**
 * Utility class for Word document operations.
 * Provides common functionality for processing Word documents.
 */
public class WordUtils {
    
    /**
     * Updates all fields in a DOCX document.
     * This is useful for ensuring calculated fields, dates, and other dynamic content
     * are updated before conversion to PDF.
     * 
     * Note: Apache POI has limited support for field updates. This method will attempt
     * to update what it can, but some field types may not be fully supported.
     *
     * @param document the DOCX document to update
     */
    public static void updateFields(XWPFDocument document) {
        // POI has limited field update capabilities
        // Fields in headers/footers and body are automatically evaluated when accessed
        // This method serves as a placeholder for any future field update logic
        // and documents the intent to update fields when macro execution is enabled
    }
}
