package com.xtopdf.xtopdf.services;

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

@Service
@Slf4j
public class PageNumberService {
    
    private static final float MARGIN = 36; // 0.5 inch margin
    private static final float DEFAULT_FONT_SIZE = 12;
    
    public void addPageNumbers(File pdfFile, PageNumberConfig config) throws IOException {
        if (!config.isEnabled()) {
            return; // Page numbering not enabled
        }
        
        // Create a temporary file for the modified PDF
        File tempFile = File.createTempFile("temp_", ".pdf");
        
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
        if (pdfFile.delete()) {
            if (!tempFile.renameTo(pdfFile)) {
                throw new IOException("Failed to replace original PDF with page numbers");
            }
        } else {
            tempFile.delete();
            throw new IOException("Failed to delete original PDF");
        }
    }
    
    private String formatPageNumber(int pageNum, PageNumberStyle style) {
        return switch (style) {
            case ARABIC -> String.valueOf(pageNum);
            case ROMAN_UPPER -> toRomanNumeral(pageNum, true);
            case ROMAN_LOWER -> toRomanNumeral(pageNum, false);
            case ALPHABETIC_UPPER -> toAlphabetic(pageNum, true);
            case ALPHABETIC_LOWER -> toAlphabetic(pageNum, false);
        };
    }
    
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
