package com.xtopdf.xtopdf.services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.xtopdf.xtopdf.config.PageNumberConfig;
import com.xtopdf.xtopdf.enums.PageNumberAlignment;
import com.xtopdf.xtopdf.enums.PageNumberPosition;
import com.xtopdf.xtopdf.enums.PageNumberStyle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
@Slf4j
public class PageNumberService {
    
    private static final float MARGIN = 36; // 0.5 inch margin
    private static final float PAGE_NUMBER_HEIGHT = 20;
    
    public void addPageNumbers(File pdfFile, PageNumberConfig config) throws IOException {
        if (!config.isEnabled()) {
            return; // Page numbering not enabled
        }
        
        // Create a temporary file for the modified PDF
        File tempFile = File.createTempFile("temp_", ".pdf");
        
        try (PdfDocument pdfDoc = new PdfDocument(new PdfReader(pdfFile), new PdfWriter(tempFile))) {
            int numberOfPages = pdfDoc.getNumberOfPages();
            
            for (int i = 1; i <= numberOfPages; i++) {
                var page = pdfDoc.getPage(i);
                Rectangle pageSize = page.getPageSize();
                
                // Determine position
                float yPosition;
                if (config.getPosition() == PageNumberPosition.TOP) {
                    yPosition = pageSize.getTop() - MARGIN;
                } else {
                    yPosition = MARGIN;
                }
                
                // Create canvas for the page
                PdfCanvas pdfCanvas = new PdfCanvas(page);
                Canvas canvas = new Canvas(pdfCanvas, pageSize);
                
                // Create paragraph with page number
                String pageNumber = formatPageNumber(i, config.getStyle());
                Paragraph paragraph = new Paragraph(pageNumber);
                
                // Set alignment
                TextAlignment textAlignment = getTextAlignment(config.getAlignment());
                paragraph.setTextAlignment(textAlignment);
                
                // Calculate x position based on alignment
                float xPosition = MARGIN;
                float width = pageSize.getWidth() - (2 * MARGIN);
                
                if (config.getAlignment() == PageNumberAlignment.CENTER) {
                    xPosition = MARGIN;
                } else if (config.getAlignment() == PageNumberAlignment.RIGHT) {
                    xPosition = MARGIN;
                } else { // LEFT
                    xPosition = MARGIN;
                }
                
                // Position the paragraph
                if (config.getPosition() == PageNumberPosition.TOP) {
                    paragraph.setFixedPosition(xPosition, yPosition - PAGE_NUMBER_HEIGHT, width);
                } else {
                    paragraph.setFixedPosition(xPosition, yPosition, width);
                }
                
                canvas.add(paragraph);
                canvas.close();
            }
        }
        
        // Replace original file with the modified one
        if (pdfFile.delete()) {
            if (!tempFile.renameTo(pdfFile)) {
                throw new IOException("Failed to replace original PDF with numbered version");
            }
        } else {
            tempFile.delete();
            throw new IOException("Failed to delete original PDF");
        }
    }
    
    private String formatPageNumber(int pageNumber, PageNumberStyle style) {
        return switch (style) {
            case ARABIC -> String.valueOf(pageNumber);
            case ROMAN_UPPER -> toRomanNumeral(pageNumber).toUpperCase();
            case ROMAN_LOWER -> toRomanNumeral(pageNumber).toLowerCase();
            case ALPHABETIC_UPPER -> toAlphabetic(pageNumber).toUpperCase();
            case ALPHABETIC_LOWER -> toAlphabetic(pageNumber).toLowerCase();
        };
    }
    
    private String toRomanNumeral(int number) {
        if (number < 1 || number > 3999) {
            return String.valueOf(number); // Fallback for out of range
        }
        
        String[] thousands = {"", "M", "MM", "MMM"};
        String[] hundreds = {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
        String[] tens = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
        String[] ones = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};
        
        return thousands[number / 1000] +
               hundreds[(number % 1000) / 100] +
               tens[(number % 100) / 10] +
               ones[number % 10];
    }
    
    private String toAlphabetic(int number) {
        if (number < 1) {
            return String.valueOf(number);
        }
        
        StringBuilder result = new StringBuilder();
        int n = number - 1; // Convert to 0-based index
        
        while (n >= 0) {
            result.insert(0, (char) ('a' + (n % 26)));
            n = (n / 26) - 1;
        }
        
        return result.toString();
    }
    
    private TextAlignment getTextAlignment(PageNumberAlignment alignment) {
        return switch (alignment) {
            case LEFT -> TextAlignment.LEFT;
            case CENTER -> TextAlignment.CENTER;
            case RIGHT -> TextAlignment.RIGHT;
        };
    }
}
