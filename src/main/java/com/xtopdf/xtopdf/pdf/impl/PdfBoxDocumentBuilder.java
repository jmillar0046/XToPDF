package com.xtopdf.xtopdf.pdf.impl;

import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Apache PDFBox implementation of the PdfDocumentBuilder interface.
 * This implementation uses PDFBox 3.x to generate PDF documents.
 * 
 * <p>PDFBox is licensed under Apache License 2.0, making it suitable
 * for commercial use without source code disclosure requirements.</p>
 * 
 * <p><strong>Note:</strong> Currently uses Helvetica font which has limited Unicode support.
 * TODO: Implement proper Unicode font support (see issue for font handling).</p>
 */
public class PdfBoxDocumentBuilder implements PdfDocumentBuilder {
    
    private static final float DEFAULT_FONT_SIZE = 12f;
    private static final float DEFAULT_LEADING = 14.5f;
    private static final float DEFAULT_MARGIN = 50f;
    private static final float TABLE_CELL_PADDING = 5f;
    
    private final PDDocument document;
    private PDPage currentPage;
    private PDPageContentStream contentStream;
    private float currentY;
    private final PDFont defaultFont;
    
    /**
     * Creates a new PDFBox document builder.
     * 
     * TODO: Add proper Unicode font support (NotoSansCJK or similar) for full international character support.
     *       Currently using Helvetica which only supports WinAnsi characters.
     * 
     * @throws IOException if the document cannot be created
     */
    public PdfBoxDocumentBuilder() throws IOException {
        this.document = new PDDocument();
        this.defaultFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        newPage();
    }
    
    @Override
    public void newPage() {
        try {
            if (contentStream != null) {
                contentStream.close();
            }
            currentPage = new PDPage(PDRectangle.A4);
            document.addPage(currentPage);
            contentStream = new PDPageContentStream(document, currentPage);
            currentY = currentPage.getMediaBox().getHeight() - DEFAULT_MARGIN;
        } catch (IOException e) {
            throw new RuntimeException("Failed to create new page", e);
        }
    }
    
    @Override
    public void addText(String text, float x, float y) throws IOException {
        if (text == null || text.isEmpty()) {
            return;
        }
        text = filterUnsupportedGlyphs(text);
        contentStream.beginText();
        contentStream.setFont(defaultFont, DEFAULT_FONT_SIZE);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }
    
    @Override
    public void addParagraph(String text) throws IOException {
        if (text == null || text.isEmpty()) {
            return;
        }
        
        // Replace tabs with spaces for consistent rendering
        text = text.replace("\t", "    ");
        
        // Filter out unsupported glyphs
        text = filterUnsupportedGlyphs(text);
        
        contentStream.beginText();
        contentStream.setFont(defaultFont, DEFAULT_FONT_SIZE);
        contentStream.setLeading(DEFAULT_LEADING);
        contentStream.newLineAtOffset(DEFAULT_MARGIN, currentY);
        
        float maxWidth = currentPage.getMediaBox().getWidth() - (2 * DEFAULT_MARGIN);
        List<String> lines = wrapText(text, maxWidth, defaultFont, DEFAULT_FONT_SIZE);
        
        for (String line : lines) {
            // Check if we need a new page
            if (currentY < DEFAULT_MARGIN) {
                contentStream.endText();
                newPage();
                contentStream.beginText();
                contentStream.setFont(defaultFont, DEFAULT_FONT_SIZE);
                contentStream.setLeading(DEFAULT_LEADING);
                contentStream.newLineAtOffset(DEFAULT_MARGIN, currentY);
            }
            
            contentStream.showText(line);
            contentStream.newLine();
            currentY -= DEFAULT_LEADING;
        }
        
        contentStream.endText();
        currentY -= DEFAULT_LEADING; // Add extra space after paragraph
    }
    
    @Override
    public void addTable(String[][] data) throws IOException {
        if (data == null || data.length == 0) {
            return;
        }
        
        int numCols = data[0].length;
        float pageWidth = currentPage.getMediaBox().getWidth() - (2 * DEFAULT_MARGIN);
        float cellWidth = pageWidth / numCols;
        float cellHeight = 20f;
        
        for (String[] row : data) {
            // Check if we need a new page
            if (currentY - cellHeight < DEFAULT_MARGIN) {
                newPage();
            }
            
            float x = DEFAULT_MARGIN;
            float y = currentY;
            
            // Draw cell borders and text
            for (int col = 0; col < numCols && col < row.length; col++) {
                // Draw cell border
                contentStream.addRect(x, y - cellHeight, cellWidth, cellHeight);
                contentStream.stroke();
                
                // Draw cell text - filter unsupported glyphs
                String cellText = row[col] != null ? row[col] : "";
                cellText = filterUnsupportedGlyphs(cellText);
                cellText = truncateText(cellText, cellWidth - (2 * TABLE_CELL_PADDING), defaultFont, DEFAULT_FONT_SIZE);
                
                contentStream.beginText();
                contentStream.setFont(defaultFont, DEFAULT_FONT_SIZE);
                contentStream.newLineAtOffset(x + TABLE_CELL_PADDING, y - cellHeight + TABLE_CELL_PADDING);
                contentStream.showText(cellText);
                contentStream.endText();
                
                x += cellWidth;
            }
            
            currentY -= cellHeight;
        }
        
        currentY -= DEFAULT_LEADING; // Add space after table
    }
    
    @Override
    public void addImage(byte[] imageData) throws IOException {
        if (imageData == null || imageData.length == 0) {
            return;
        }
        
        PDImageXObject image = PDImageXObject.createFromByteArray(document, imageData, "image");
        
        float pageWidth = currentPage.getMediaBox().getWidth() - (2 * DEFAULT_MARGIN);
        float pageHeight = currentPage.getMediaBox().getHeight() - (2 * DEFAULT_MARGIN);
        float imageWidth = image.getWidth();
        float imageHeight = image.getHeight();
        
        // Calculate scaling to fit page while maintaining aspect ratio
        float scale = Math.min(pageWidth / imageWidth, pageHeight / imageHeight);
        if (scale > 1) scale = 1; // Don't upscale
        
        float scaledWidth = imageWidth * scale;
        float scaledHeight = imageHeight * scale;
        
        // Check if we need a new page
        if (currentY - scaledHeight < DEFAULT_MARGIN) {
            newPage();
        }
        
        contentStream.drawImage(image, DEFAULT_MARGIN, currentY - scaledHeight, 
                               scaledWidth, scaledHeight);
        currentY -= (scaledHeight + DEFAULT_LEADING);
    }
    
    @Override
    public void drawLine(float x1, float y1, float x2, float y2) throws IOException {
        contentStream.moveTo(x1, y1);
        contentStream.lineTo(x2, y2);
        contentStream.stroke();
    }
    
    @Override
    public void drawCircle(float cx, float cy, float radius) throws IOException {
        // PDFBox doesn't have a direct circle method
        // Approximate circle with 4 Bezier curves
        float k = 0.552284749831f; // Magic constant for circle approximation
        float kr = k * radius;
        
        contentStream.moveTo(cx, cy + radius);
        contentStream.curveTo(cx + kr, cy + radius, cx + radius, cy + kr, cx + radius, cy);
        contentStream.curveTo(cx + radius, cy - kr, cx + kr, cy - radius, cx, cy - radius);
        contentStream.curveTo(cx - kr, cy - radius, cx - radius, cy - kr, cx - radius, cy);
        contentStream.curveTo(cx - radius, cy + kr, cx - kr, cy + radius, cx, cy + radius);
        contentStream.stroke();
    }
    
    @Override
    public void drawRectangle(float x, float y, float width, float height) throws IOException {
        contentStream.addRect(x, y, width, height);
        contentStream.stroke();
    }
    
    @Override
    public void save(File outputFile) throws IOException {
        if (contentStream != null) {
            contentStream.close();
            contentStream = null;
        }
        document.save(outputFile);
    }
    
    @Override
    public void close() throws IOException {
        if (contentStream != null) {
            contentStream.close();
        }
        if (document != null) {
            document.close();
        }
    }
    
    /**
     * Wraps text to fit within the specified width.
     * 
     * @param text The text to wrap
     * @param maxWidth Maximum width in points
     * @param font The font to use
     * @param fontSize The font size
     * @return List of wrapped lines
     * @throws IOException if text width cannot be calculated
     */
    private List<String> wrapText(String text, float maxWidth, PDFont font, float fontSize) throws IOException {
        List<String> lines = new ArrayList<>();
        String[] paragraphs = text.split("\n");
        
        for (String paragraph : paragraphs) {
            if (paragraph.isEmpty()) {
                lines.add("");
                continue;
            }
            
            String[] words = paragraph.split(" ");
            StringBuilder line = new StringBuilder();
            
            for (String word : words) {
                String testLine = line.length() == 0 ? word : line + " " + word;
                float width = font.getStringWidth(testLine) / 1000 * fontSize;
                
                if (width > maxWidth && line.length() > 0) {
                    lines.add(line.toString());
                    line = new StringBuilder(word);
                } else {
                    line = new StringBuilder(testLine);
                }
            }
            
            if (line.length() > 0) {
                lines.add(line.toString());
            }
        }
        
        return lines;
    }
    
    /**
     * Truncates text to fit within the specified width.
     * 
     * @param text The text to truncate
     * @param maxWidth Maximum width in points
     * @param font The font to use
     * @param fontSize The font size
     * @return Truncated text with "..." if necessary
     * @throws IOException if text width cannot be calculated
     */
    private String truncateText(String text, float maxWidth, PDFont font, float fontSize) throws IOException {
        float width = font.getStringWidth(text) / 1000 * fontSize;
        
        if (width <= maxWidth) {
            return text;
        }
        
        // Truncate and add ellipsis
        String ellipsis = "...";
        float ellipsisWidth = font.getStringWidth(ellipsis) / 1000 * fontSize;
        
        StringBuilder truncated = new StringBuilder();
        for (char c : text.toCharArray()) {
            String test = truncated.toString() + c;
            float testWidth = font.getStringWidth(test) / 1000 * fontSize;
            
            if (testWidth + ellipsisWidth > maxWidth) {
                break;
            }
            truncated.append(c);
        }
        
        return truncated.toString() + ellipsis;
    }
    
    /**
     * Filters out characters not supported by Helvetica font.
     * Helvetica (Type1) only supports WinAnsiEncoding (code points 32-126 and 128-255).
     * 
     * TODO: Remove this filtering once proper Unicode font is implemented.
     * 
     * @param text The text to filter
     * @return Filtered text with unsupported characters replaced with '?'
     */
    private String filterUnsupportedGlyphs(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        StringBuilder filtered = new StringBuilder();
        for (char c : text.toCharArray()) {
            // Allow basic ASCII (32-126), extended ASCII (128-255), and whitespace
            if ((c >= 32 && c <= 126) || (c >= 128 && c <= 255) || Character.isWhitespace(c)) {
                filtered.append(c);
            } else {
                // Replace unsupported characters with '?'
                filtered.append('?');
            }
        }
        
        return filtered.toString();
    }
    
    @Override
    public void newPage(float width, float height) throws IOException {
        if (contentStream != null) {
            contentStream.close();
        }
        currentPage = new PDPage(new PDRectangle(width, height));
        document.addPage(currentPage);
        contentStream = new PDPageContentStream(document, currentPage);
        currentY = height - DEFAULT_MARGIN;
    }
    
    @Override
    public void drawArc(float cx, float cy, float radius, float startAngle, float sweepAngle) throws IOException {
        // Convert angles from degrees to radians
        double startRad = Math.toRadians(startAngle);
        double endRad = Math.toRadians(startAngle + sweepAngle);
        
        // Draw arc using Bezier curve approximation
        int segments = Math.max(1, (int)(Math.abs(sweepAngle) / 90) * 4);
        double angleStep = (endRad - startRad) / segments;
        
        float prevX = (float)(cx + radius * Math.cos(startRad));
        float prevY = (float)(cy + radius * Math.sin(startRad));
        
        for (int i = 1; i <= segments; i++) {
            double angle = startRad + i * angleStep;
            float x = (float)(cx + radius * Math.cos(angle));
            float y = (float)(cy + radius * Math.sin(angle));
            contentStream.moveTo(prevX, prevY);
            contentStream.lineTo(x, y);
            contentStream.stroke();
            prevX = x;
            prevY = y;
        }
    }
    
    @Override
    public void drawEllipse(float cx, float cy, float radiusX, float radiusY) throws IOException {
        // Draw ellipse using Bezier curve approximation (4 segments)
        float kappa = 0.5522848f; // Magic constant for Bezier circle approximation
        float ox = radiusX * kappa; // Control point offset X
        float oy = radiusY * kappa; // Control point offset Y
        
        // Starting point (right)
        contentStream.moveTo(cx + radiusX, cy);
        
        // Top-right quadrant
        contentStream.curveTo(
            cx + radiusX, cy + oy,
            cx + ox, cy + radiusY,
            cx, cy + radiusY
        );
        
        // Top-left quadrant
        contentStream.curveTo(
            cx - ox, cy + radiusY,
            cx - radiusX, cy + oy,
            cx - radiusX, cy
        );
        
        // Bottom-left quadrant
        contentStream.curveTo(
            cx - radiusX, cy - oy,
            cx - ox, cy - radiusY,
            cx, cy - radiusY
        );
        
        // Bottom-right quadrant
        contentStream.curveTo(
            cx + ox, cy - radiusY,
            cx + radiusX, cy - oy,
            cx + radiusX, cy
        );
        
        contentStream.stroke();
    }
    
    @Override
    public void fillRectangle(float x, float y, float width, float height) throws IOException {
        contentStream.addRect(x, y, width, height);
        contentStream.fill();
    }
    
    @Override
    public void drawPolygon(float[] xPoints, float[] yPoints, int nPoints, boolean filled) throws IOException {
        if (nPoints < 2) {
            return;
        }
        
        // Move to first point
        contentStream.moveTo(xPoints[0], yPoints[0]);
        
        // Draw lines to remaining points
        for (int i = 1; i < nPoints; i++) {
            contentStream.lineTo(xPoints[i], yPoints[i]);
        }
        
        // Close the path
        contentStream.closePath();
        
        if (filled) {
            contentStream.fillAndStroke();
        } else {
            contentStream.stroke();
        }
    }
    
    @Override
    public void setStrokeColor(int r, int g, int b) throws IOException {
        contentStream.setStrokingColor(r / 255f, g / 255f, b / 255f);
    }
    
    @Override
    public void setFillColor(int r, int g, int b) throws IOException {
        contentStream.setNonStrokingColor(r / 255f, g / 255f, b / 255f);
    }
    
    @Override
    public void setLineWidth(float width) throws IOException {
        contentStream.setLineWidth(width);
    }
    
    @Override
    public void setLineDash(float dashLength, float gapLength) throws IOException {
        contentStream.setLineDashPattern(new float[]{dashLength, gapLength}, 0);
    }
    
    @Override
    public void resetLineDash() throws IOException {
        contentStream.setLineDashPattern(new float[]{}, 0);
    }
    
    @Override
    public void saveState() throws IOException {
        contentStream.saveGraphicsState();
    }
    
    @Override
    public void restoreState() throws IOException {
        contentStream.restoreGraphicsState();
    }
}
