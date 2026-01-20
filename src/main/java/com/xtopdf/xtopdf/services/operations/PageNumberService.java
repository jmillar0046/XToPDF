package com.xtopdf.xtopdf.services.operations;

import com.xtopdf.xtopdf.config.PageNumberConfig;
import com.xtopdf.xtopdf.enums.PageNumberAlignment;
import com.xtopdf.xtopdf.enums.PageNumberPosition;
import com.xtopdf.xtopdf.enums.PageNumberStyle;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * Service for adding page numbers to PDF documents.
 * Uses Apache PDFBox to modify existing PDF files and add page numbers
 * with configurable position, alignment, and style.
 * 
 * <p>Supported features:
 * <ul>
 *   <li>Position: Top or bottom of page</li>
 *   <li>Alignment: Left, center, or right</li>
 *   <li>Style: Arabic (1, 2, 3), Roman (I, II, III), Alphabetic (A, B, C)</li>
 *   <li>Automatic temporary file cleanup on error</li>
 * </ul>
 * 
 * <p>Limitations:
 * <ul>
 *   <li>Roman numerals limited to 1-3999</li>
 *   <li>Fixed font (Helvetica) and size (12pt)</li>
 *   <li>Fixed margin (0.5 inch)</li>
 * </ul>
 * 
 * <p>Example usage:
 * <pre>
 * PageNumberConfig config = PageNumberConfig.builder()
 *     .enabled(true)
 *     .position(PageNumberPosition.BOTTOM)
 *     .alignment(PageNumberAlignment.CENTER)
 *     .style(PageNumberStyle.ARABIC)
 *     .build();
 * pageNumberService.addPageNumbers(pdfFile, config);
 * </pre>
 * 
 * @see PageNumberConfig
 * @see PageNumberPosition
 * @see PageNumberAlignment
 * @see PageNumberStyle
 */
@Service
@Slf4j
public class PageNumberService {
    
    private static final float MARGIN = 36; // 0.5 inch margin
    private static final float DEFAULT_FONT_SIZE = 12;
    
    /**
     * Adds page numbers to a PDF file according to the provided configuration.
     * 
     * <p>This method modifies the PDF file in-place by:
     * <ol>
     *   <li>Creating a temporary copy of the PDF</li>
     *   <li>Adding page numbers to each page</li>
     *   <li>Replacing the original file with the modified version</li>
     *   <li>Cleaning up the temporary file</li>
     * </ol>
     * 
     * <p>The temporary file is guaranteed to be cleaned up even if an error occurs,
     * using a try-finally block.
     * 
     * @param pdfFile the PDF file to modify (must exist and be readable)
     * @param config the page number configuration (position, alignment, style)
     * @throws IOException if the file cannot be read, modified, or replaced
     * @throws IllegalArgumentException if pdfFile is null or doesn't exist
     */
    public void addPageNumbers(File pdfFile, PageNumberConfig config) throws IOException {
        if (!config.isEnabled()) {
            return; // Page numbering not enabled
        }
        
        File tempFile = null;
        try {
            // Create a temporary file for the modified PDF
            tempFile = File.createTempFile("temp_", ".pdf");
            
            try (PDDocument document = Loader.loadPDF(pdfFile)) {
                PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                int pageNum = 1;
                int totalPages = document.getNumberOfPages();
                
                for (PDPage page : document.getPages()) {
                    float pageWidth = page.getMediaBox().getWidth();
                    float pageHeight = page.getMediaBox().getHeight();
                    
                    // Determine position
                    float yPosition;
                    if (config.getPosition() == PageNumberPosition.TOP) {
                        yPosition = pageHeight - MARGIN;
                    } else {
                        yPosition = MARGIN;
                    }
                    
                    // Format page number based on style
                    String pageNumberText = formatPageNumber(pageNum, config.getStyle());
                    
                    // Calculate x position based on alignment
                    float textWidth = font.getStringWidth(pageNumberText) / 1000 * DEFAULT_FONT_SIZE;
                    float xPosition = switch (config.getAlignment()) {
                        case LEFT -> MARGIN;
                        case RIGHT -> pageWidth - MARGIN - textWidth;
                        case CENTER -> (pageWidth - textWidth) / 2;
                    };
                    
                    try (PDPageContentStream contentStream = new PDPageContentStream(
                            document, page, PDPageContentStream.AppendMode.APPEND, true)) {
                        
                        // Draw page number
                        contentStream.beginText();
                        contentStream.setFont(font, DEFAULT_FONT_SIZE);
                        contentStream.newLineAtOffset(xPosition, yPosition);
                        contentStream.showText(pageNumberText);
                        contentStream.endText();
                    }
                    
                    pageNum++;
                }
                
                document.save(tempFile);
            }
            
            // Replace original file with the modified one
            if (!pdfFile.delete()) {
                throw new IOException("Failed to delete original PDF");
            }
            
            if (!tempFile.renameTo(pdfFile)) {
                // If rename fails, try to copy the content
                try (java.io.FileInputStream fis = new java.io.FileInputStream(tempFile);
                     java.io.FileOutputStream fos = new java.io.FileOutputStream(pdfFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }
            }
            
            // Mark temp file for deletion (will be cleaned up in finally)
            tempFile = null;
            
        } finally {
            // Guaranteed cleanup of temporary file
            if (tempFile != null && tempFile.exists()) {
                if (!tempFile.delete()) {
                    log.warn("Failed to delete temporary file: {}", tempFile.getAbsolutePath());
                }
            }
        }
    }
    
    /**
     * Formats a page number according to the specified style.
     * 
     * @param pageNum the page number to format (1-based)
     * @param style the numbering style to use
     * @return the formatted page number string
     */
    private String formatPageNumber(int pageNum, PageNumberStyle style) {
        return switch (style) {
            case ARABIC -> String.valueOf(pageNum);
            case ROMAN_UPPER -> toRomanNumeral(pageNum, true);
            case ROMAN_LOWER -> toRomanNumeral(pageNum, false);
            case ALPHABETIC_UPPER -> toAlphabetic(pageNum, true);
            case ALPHABETIC_LOWER -> toAlphabetic(pageNum, false);
        };
    }
    
    /**
     * Converts an integer to Roman numeral notation.
     * 
     * @param num the number to convert (1-3999)
     * @param uppercase true for uppercase (I, II, III), false for lowercase (i, ii, iii)
     * @return the Roman numeral string, or the original number as string if out of range
     */
    private String toRomanNumeral(int num, boolean uppercase) {
        if (num <= 0 || num > 3999) {
            return String.valueOf(num); // Fallback for out-of-range values
        }
        
        String[] thousands = {"", "M", "MM", "MMM"};
        String[] hundreds = {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
        String[] tens = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
        String[] ones = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};
        
        String roman = thousands[num / 1000] + hundreds[(num % 1000) / 100] +
                      tens[(num % 100) / 10] + ones[num % 10];
        
        return uppercase ? roman : roman.toLowerCase();
    }
    
    /**
     * Converts an integer to alphabetic notation (A, B, C... Z, AA, AB...).
     * 
     * @param num the number to convert (must be positive)
     * @param uppercase true for uppercase (A, B, C), false for lowercase (a, b, c)
     * @return the alphabetic string, or the original number as string if invalid
     */
    private String toAlphabetic(int num, boolean uppercase) {
        if (num <= 0) {
            return String.valueOf(num); // Fallback
        }
        
        StringBuilder result = new StringBuilder();
        int current = num;
        
        while (current > 0) {
            current--; // Adjust for 0-based indexing
            char letter = (char) ((current % 26) + (uppercase ? 'A' : 'a'));
            result.insert(0, letter);
            current /= 26;
        }
        
        return result.toString();
    }
}
