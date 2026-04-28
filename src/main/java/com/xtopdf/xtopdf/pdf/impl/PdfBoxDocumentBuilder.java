package com.xtopdf.xtopdf.pdf.impl;

import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import com.xtopdf.xtopdf.pdf.TextAlignment;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Apache PDFBox implementation of the PdfDocumentBuilder interface.
 * This implementation uses PDFBox 3.x to generate PDF documents.
 *
 * <p>PDFBox is licensed under Apache License 2.0, making it suitable
 * for commercial use without source code disclosure requirements.</p>
 *
 * <p>Loads NotoSans fonts from the classpath for full Unicode support
 * (Latin, Cyrillic, CJK). Falls back to Helvetica if fonts cannot be loaded.</p>
 */
@Slf4j
public class PdfBoxDocumentBuilder implements PdfDocumentBuilder {

    private static final float DEFAULT_FONT_SIZE = 12f;
    private static final float DEFAULT_LEADING = 14.5f;
    private static final float DEFAULT_MARGIN = 50f;
    private static final float TABLE_CELL_PADDING = 5f;
    private static final float HEADER_FOOTER_FONT_SIZE = 10f;
    private static final float HEADER_Y_OFFSET = 25f;
    private static final float FOOTER_Y_POSITION = 25f;

    private final PDDocument document;
    private PDPage currentPage;
    private PDPageContentStream contentStream;
    private float currentY;
    private boolean fontsLoaded = false;

    // NotoSans font variants
    private PDFont regularFont;
    private PDFont boldFont;
    private PDFont cjkFont;

    // Formatted text accumulation for addFormattedText / endParagraph
    private final List<TextSegment> pendingSegments = new ArrayList<>();

    // Current paragraph alignment (resets to LEFT after each endParagraph)
    private TextAlignment currentAlignment = TextAlignment.LEFT;

    /**
     * A segment of formatted text accumulated via {@link #addFormattedText}.
     */
    record TextSegment(String text, boolean bold, boolean italic, float fontSize) {}

    /**
     * Creates a new PDFBox document builder.
     * Loads NotoSans fonts from the classpath for Unicode support.
     * Falls back to Helvetica if any font fails to load.
     *
     * @throws IOException if the document cannot be created
     */
    public PdfBoxDocumentBuilder() throws IOException {
        this.document = new PDDocument();
        loadFonts();
        newPage();
    }

    /**
     * Attempts to load NotoSans fonts from the classpath.
     * On success, sets {@code fontsLoaded = true} and assigns {@code regularFont} and {@code boldFont}.
     * On failure, falls back to Helvetica and logs a warning.
     */
    private void loadFonts() {
        try {
            // Load regular and bold TTF fonts
            try (InputStream regularStream = getClass().getResourceAsStream("/fonts/NotoSans-Regular.ttf");
                 InputStream boldStream = getClass().getResourceAsStream("/fonts/NotoSans-Bold.ttf")) {

                if (regularStream == null || boldStream == null) {
                    throw new IOException("NotoSans-Regular.ttf or NotoSans-Bold.ttf not found on classpath");
                }

                this.regularFont = PDType0Font.load(document, regularStream, false);
                this.boldFont = PDType0Font.load(document, boldStream, false);
                this.fontsLoaded = true;
                log.debug("NotoSans regular and bold fonts loaded successfully");
            }

            // Load CJK font separately — OTF with CFF outlines requires special handling
            loadCjkFont();

        } catch (IOException e) {
            log.warn("Failed to load NotoSans fonts from classpath, falling back to Helvetica: {}", e.getMessage());
            this.regularFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            this.boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            this.cjkFont = null;
            this.fontsLoaded = false;
        }
    }

    /**
     * Attempts to load the CJK font from the classpath.
     * Tries OTF first, then falls back to TTF variant if available.
     * If CJK font cannot be loaded, logs a warning and continues without it.
     */
    private void loadCjkFont() {
        // Try loading as OTF with CFF outlines (PDFBox may not support this)
        try (InputStream cjkStream = getClass().getResourceAsStream("/fonts/NotoSansCJK-Regular.otf")) {
            if (cjkStream != null) {
                this.cjkFont = PDType0Font.load(document, cjkStream, false);
                log.debug("NotoSansCJK font loaded successfully");
                return;
            }
        } catch (IOException e) {
            log.debug("Could not load NotoSansCJK-Regular.otf (CFF outlines not supported by PDFBox): {}", e.getMessage());
        }

        // Try TTF variant as fallback
        try (InputStream cjkTtfStream = getClass().getResourceAsStream("/fonts/NotoSansCJK-Regular.ttf")) {
            if (cjkTtfStream != null) {
                this.cjkFont = PDType0Font.load(document, cjkTtfStream, false);
                log.debug("NotoSansCJK TTF font loaded successfully");
                return;
            }
        } catch (IOException e) {
            log.debug("Could not load NotoSansCJK-Regular.ttf: {}", e.getMessage());
        }

        // Try NotoSansSC (Simplified Chinese) TTF as another fallback
        try (InputStream scStream = getClass().getResourceAsStream("/fonts/NotoSansSC-Regular.ttf")) {
            if (scStream != null) {
                this.cjkFont = PDType0Font.load(document, scStream, false);
                log.debug("NotoSansSC TTF font loaded as CJK fallback");
                return;
            }
        } catch (IOException e) {
            log.debug("Could not load NotoSansSC-Regular.ttf: {}", e.getMessage());
        }

        log.warn("No CJK font could be loaded; CJK characters may not render correctly");
        this.cjkFont = null;
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
        text = safeEncode(text, regularFont);
        contentStream.beginText();
        contentStream.setFont(regularFont, DEFAULT_FONT_SIZE);
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
        text = safeEncode(text, regularFont);

        contentStream.beginText();
        contentStream.setFont(regularFont, DEFAULT_FONT_SIZE);
        contentStream.setLeading(DEFAULT_LEADING);
        contentStream.newLineAtOffset(DEFAULT_MARGIN, currentY);

        float maxWidth = currentPage.getMediaBox().getWidth() - (2 * DEFAULT_MARGIN);
        List<String> lines = wrapText(text, maxWidth, regularFont, DEFAULT_FONT_SIZE);

        for (String line : lines) {
            // Check if we need a new page
            if (currentY < DEFAULT_MARGIN) {
                contentStream.endText();
                newPage();
                contentStream.beginText();
                contentStream.setFont(regularFont, DEFAULT_FONT_SIZE);
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

                // Draw cell text
                String cellText = row[col] != null ? row[col] : "";
                cellText = safeEncode(cellText, regularFont);
                cellText = truncateText(cellText, cellWidth - (2 * TABLE_CELL_PADDING), regularFont, DEFAULT_FONT_SIZE);

                contentStream.beginText();
                contentStream.setFont(regularFont, DEFAULT_FONT_SIZE);
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
        float k = 0.552284749831f;
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

    /**
     * Returns whether NotoSans fonts were successfully loaded from the classpath.
     * Package-private for testing.
     *
     * @return true if NotoSans fonts are loaded, false if using Helvetica fallback
     */
    boolean isFontsLoaded() {
        return fontsLoaded;
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

    // ---------------------------------------------------------------
    // Task 2.6: addFormattedText / endParagraph
    // ---------------------------------------------------------------

    @Override
    public void setAlignment(TextAlignment alignment) throws IOException {
        this.currentAlignment = alignment != null ? alignment : TextAlignment.LEFT;
    }

    @Override
    public void addFormattedText(String text, boolean bold, boolean italic, float fontSize) throws IOException {
        if (text == null || text.isEmpty()) {
            return;
        }
        float effectiveFontSize = fontSize <= 0 ? DEFAULT_FONT_SIZE : fontSize;
        pendingSegments.add(new TextSegment(text, bold, italic, effectiveFontSize));
    }

    @Override
    public void endParagraph() throws IOException {
        if (pendingSegments.isEmpty()) {
            return;
        }

        try {
            float maxWidth = currentPage.getMediaBox().getWidth() - (2 * DEFAULT_MARGIN);

            // Build word-level tokens that remember their formatting
            List<WordToken> tokens = buildWordTokens();

            // Render lines with word-wrapping
            List<LineOfTokens> lines = wrapTokensIntoLines(tokens, maxWidth);

            for (LineOfTokens line : lines) {
                if (currentY < DEFAULT_MARGIN) {
                    newPage();
                }
                renderLine(line, maxWidth);
                currentY -= DEFAULT_LEADING;
            }

            currentY -= DEFAULT_LEADING; // extra space after paragraph
        } finally {
            pendingSegments.clear();
            currentAlignment = TextAlignment.LEFT;
        }
    }

    // ---------------------------------------------------------------
    // Task 2.7: addHeaderText / addFooterText
    // ---------------------------------------------------------------

    @Override
    public void addHeaderText(String text) throws IOException {
        if (text == null || text.isEmpty()) {
            return;
        }
        text = safeEncode(text, regularFont);
        float headerY = currentPage.getMediaBox().getHeight() - HEADER_Y_OFFSET;
        contentStream.beginText();
        contentStream.setFont(regularFont, HEADER_FOOTER_FONT_SIZE);
        contentStream.newLineAtOffset(DEFAULT_MARGIN, headerY);
        contentStream.showText(text);
        contentStream.endText();
    }

    @Override
    public void addFooterText(String text) throws IOException {
        if (text == null || text.isEmpty()) {
            return;
        }
        text = safeEncode(text, regularFont);
        contentStream.beginText();
        contentStream.setFont(regularFont, HEADER_FOOTER_FONT_SIZE);
        contentStream.newLineAtOffset(DEFAULT_MARGIN, FOOTER_Y_POSITION);
        contentStream.showText(text);
        contentStream.endText();
    }

    // ---------------------------------------------------------------
    // Formatted text internals
    // ---------------------------------------------------------------

    /**
     * A single word (or whitespace-delimited token) with its associated font and size.
     */
    private record WordToken(String word, PDFont font, float fontSize) {}

    /**
     * A line of word tokens ready to be rendered.
     */
    private record LineOfTokens(List<WordToken> tokens) {}

    /**
     * Selects the appropriate font variant based on bold/italic flags.
     * Uses CJK font for characters outside the regular font's coverage.
     */
    private PDFont selectFont(boolean bold, boolean italic) {
        if (bold) {
            return boldFont;
        }
        // italic and bold-italic both fall back to their non-italic counterparts
        // since we don't ship italic NotoSans variants
        return regularFont;
    }

    /**
     * Determines the best font for a given character — uses CJK font if the
     * primary font cannot encode the character. Returns null if no font can
     * encode the character.
     */
    private PDFont fontForChar(char c, PDFont primaryFont) {
        try {
            primaryFont.encode(String.valueOf(c));
            return primaryFont;
        } catch (Exception e) {
            // Character not supported by primary font, try CJK
            if (cjkFont != null) {
                try {
                    cjkFont.encode(String.valueOf(c));
                    return cjkFont;
                } catch (Exception ex) {
                    // CJK font also can't encode
                }
            }
            // No font can encode this character — return null to signal skip
            return null;
        }
    }

    /**
     * Splits all pending segments into word-level tokens. Each token carries
     * the font resolved for its characters (with CJK fallback applied per-run
     * of contiguous characters sharing the same font).
     */
    private List<WordToken> buildWordTokens() {
        List<WordToken> tokens = new ArrayList<>();

        for (TextSegment seg : pendingSegments) {
            PDFont primaryFont = selectFont(seg.bold(), seg.italic());
            String text = seg.text();
            float fontSize = seg.fontSize();

            // Split into runs of characters that share the same resolved font,
            // then split those runs into words.
            List<FontRun> fontRuns = splitIntoFontRuns(text, primaryFont);
            for (FontRun run : fontRuns) {
                // Split the run text by spaces to get words
                String[] words = run.text().split(" ", -1);
                for (int i = 0; i < words.length; i++) {
                    String word = words[i];
                    // Re-add the space as a prefix for non-first words
                    if (i > 0) {
                        word = " " + word;
                    }
                    if (!word.isEmpty()) {
                        tokens.add(new WordToken(word, run.font(), fontSize));
                    }
                }
            }
        }
        return tokens;
    }

    /**
     * A contiguous run of text that uses the same resolved font.
     */
    private record FontRun(String text, PDFont font) {}

    /**
     * Splits text into contiguous runs where each character resolves to the same font
     * (primary vs CJK fallback). Characters that cannot be encoded by any font are skipped.
     */
    private List<FontRun> splitIntoFontRuns(String text, PDFont primaryFont) {
        List<FontRun> runs = new ArrayList<>();
        if (text.isEmpty()) {
            return runs;
        }

        StringBuilder currentRun = new StringBuilder();
        PDFont currentFont = null;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            PDFont charFont = fontForChar(c, primaryFont);
            if (charFont == null) {
                // Character can't be encoded by any font — skip it
                continue;
            }
            if (currentFont == null) {
                currentFont = charFont;
                currentRun.append(c);
            } else if (charFont == currentFont) {
                currentRun.append(c);
            } else {
                runs.add(new FontRun(currentRun.toString(), currentFont));
                currentRun = new StringBuilder();
                currentRun.append(c);
                currentFont = charFont;
            }
        }
        if (!currentRun.isEmpty()) {
            runs.add(new FontRun(currentRun.toString(), currentFont));
        }
        return runs;
    }

    /**
     * Wraps word tokens into lines that fit within maxWidth.
     */
    private List<LineOfTokens> wrapTokensIntoLines(List<WordToken> tokens, float maxWidth) throws IOException {
        List<LineOfTokens> lines = new ArrayList<>();
        List<WordToken> currentLine = new ArrayList<>();
        float currentWidth = 0;

        for (WordToken token : tokens) {
            float tokenWidth = token.font().getStringWidth(token.word()) / 1000 * token.fontSize();

            if (currentWidth + tokenWidth > maxWidth && !currentLine.isEmpty()) {
                lines.add(new LineOfTokens(new ArrayList<>(currentLine)));
                currentLine.clear();
                currentWidth = 0;
                // Strip leading space from the token that starts a new line
                if (token.word().startsWith(" ")) {
                    String trimmed = token.word().substring(1);
                    if (!trimmed.isEmpty()) {
                        token = new WordToken(trimmed, token.font(), token.fontSize());
                        tokenWidth = token.font().getStringWidth(token.word()) / 1000 * token.fontSize();
                    } else {
                        continue;
                    }
                }
            }

            currentLine.add(token);
            currentWidth += tokenWidth;
        }

        if (!currentLine.isEmpty()) {
            lines.add(new LineOfTokens(new ArrayList<>(currentLine)));
        }

        return lines;
    }

    /**
     * Renders a single line of word tokens at the current Y position,
     * applying the current alignment to compute the X offset.
     */
    private void renderLine(LineOfTokens line, float maxWidth) throws IOException {
        // Compute line width by summing all token widths
        float lineWidth = 0;
        for (WordToken token : line.tokens()) {
            lineWidth += token.font().getStringWidth(token.word()) / 1000 * token.fontSize();
        }

        // Compute X offset based on alignment
        float xOffset = switch (currentAlignment) {
            case LEFT -> DEFAULT_MARGIN;
            case CENTER -> DEFAULT_MARGIN + (maxWidth - lineWidth) / 2;
            case RIGHT -> DEFAULT_MARGIN + (maxWidth - lineWidth);
        };

        contentStream.beginText();
        contentStream.newLineAtOffset(xOffset, currentY);

        // Group consecutive tokens with the same font and size to minimize font switches
        PDFont lastFont = null;
        float lastSize = -1;

        for (WordToken token : line.tokens()) {
            if (token.font() != lastFont || token.fontSize() != lastSize) {
                contentStream.setFont(token.font(), token.fontSize());
                lastFont = token.font();
                lastSize = token.fontSize();
            }
            contentStream.showText(token.word());
        }

        contentStream.endText();
    }

    // ---------------------------------------------------------------
    // Text utilities
    // ---------------------------------------------------------------

    /**
     * Wraps text to fit within the specified width.
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
                String testLine = line.isEmpty() ? word : line + " " + word;
                float width = font.getStringWidth(testLine) / 1000 * fontSize;

                if (width > maxWidth && !line.isEmpty()) {
                    lines.add(line.toString());
                    line = new StringBuilder(word);
                } else {
                    line = new StringBuilder(testLine);
                }
            }

            if (!line.isEmpty()) {
                lines.add(line.toString());
            }
        }

        return lines;
    }

    /**
     * Filters out characters that cannot be encoded by the given font.
     * Characters that fail encoding are replaced with a space.
     */
    private String safeEncode(String text, PDFont font) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        StringBuilder safe = new StringBuilder();
        for (char c : text.toCharArray()) {
            try {
                font.encode(String.valueOf(c));
                safe.append(c);
            } catch (Exception e) {
                // Character can't be encoded — replace with space
                safe.append(' ');
            }
        }
        return safe.toString();
    }

    /**
     * Truncates text to fit within the specified width.
     */
    private String truncateText(String text, float maxWidth, PDFont font, float fontSize) throws IOException {
        float width = font.getStringWidth(text) / 1000 * fontSize;

        if (width <= maxWidth) {
            return text;
        }

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

        return truncated + ellipsis;
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
        double startRad = Math.toRadians(startAngle);
        double endRad = Math.toRadians(startAngle + sweepAngle);

        int segments = Math.max(1, (int) (Math.abs(sweepAngle) / 90) * 4);
        double angleStep = (endRad - startRad) / segments;

        float prevX = (float) (cx + radius * Math.cos(startRad));
        float prevY = (float) (cy + radius * Math.sin(startRad));

        for (int i = 1; i <= segments; i++) {
            double angle = startRad + i * angleStep;
            float x = (float) (cx + radius * Math.cos(angle));
            float y = (float) (cy + radius * Math.sin(angle));
            contentStream.moveTo(prevX, prevY);
            contentStream.lineTo(x, y);
            contentStream.stroke();
            prevX = x;
            prevY = y;
        }
    }

    @Override
    public void drawEllipse(float cx, float cy, float radiusX, float radiusY) throws IOException {
        float kappa = 0.5522848f;
        float ox = radiusX * kappa;
        float oy = radiusY * kappa;

        contentStream.moveTo(cx + radiusX, cy);
        contentStream.curveTo(cx + radiusX, cy + oy, cx + ox, cy + radiusY, cx, cy + radiusY);
        contentStream.curveTo(cx - ox, cy + radiusY, cx - radiusX, cy + oy, cx - radiusX, cy);
        contentStream.curveTo(cx - radiusX, cy - oy, cx - ox, cy - radiusY, cx, cy - radiusY);
        contentStream.curveTo(cx + ox, cy - radiusY, cx + radiusX, cy - oy, cx + radiusX, cy);
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

        contentStream.moveTo(xPoints[0], yPoints[0]);
        for (int i = 1; i < nPoints; i++) {
            contentStream.lineTo(xPoints[i], yPoints[i]);
        }
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
