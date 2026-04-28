package com.xtopdf.xtopdf.services.conversion.document;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import com.xtopdf.xtopdf.pdf.TextAlignment;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBrType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service to convert DOCX (Word) files to PDF.
 * Uses Apache POI to parse DOCX and PDFBox to generate PDF.
 *
 * <p>This implementation uses single-pass body element iteration to preserve
 * document order, extracts per-run formatting (bold, italic, font size),
 * renders embedded images, processes headers/footers, and detects bulleted
 * and numbered lists.</p>
 */
@Service
@Slf4j
public class DocxToPdfService {

    private final PdfBackendProvider pdfBackend;

    public DocxToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }

    public void convertDocxToPdf(MultipartFile docxFile, File pdfFile) throws IOException {
        try (var fis = docxFile.getInputStream();
             XWPFDocument docxDocument = new XWPFDocument(fis);
             PdfDocumentBuilder builder = pdfBackend.createBuilder()) {

            // Local variable — each invocation gets its own map (thread-safe)
            Map<String, Integer> listCounters = new HashMap<>();

            // Mutable holder for tracking whether any content has been rendered.
            // Used to prevent blank leading pages when a page break appears before any content.
            boolean[] contentRendered = {false};

            // Process headers before body content
            processHeaders(docxDocument, builder);

            // Single-pass body element iteration (Task 4.6)
            for (IBodyElement element : docxDocument.getBodyElements()) {
                if (element instanceof XWPFParagraph paragraph) {
                    processParagraph(paragraph, builder, listCounters, contentRendered);
                } else if (element instanceof XWPFTable table) {
                    processTable(table, builder);
                }
            }

            // Process footers after body content
            processFooters(docxDocument, builder);

            builder.save(pdfFile);
        } catch (Exception e) {
            log.error("Error processing DOCX file: {}", e.getMessage(), e);
            throw new IOException("Error processing DOCX file: " + e.getMessage(), e);
        }
    }

    /**
     * Processes a single paragraph, extracting per-run formatting, embedded images,
     * list detection, and page break handling.
     */
    private void processParagraph(XWPFParagraph paragraph, PdfDocumentBuilder builder,
                                  Map<String, Integer> listCounters, boolean[] contentRendered) throws IOException {
        // Check for paragraph-level page break (paragraph.isPageBreak() checks if the
        // paragraph has a page break before it)
        try {
            if (paragraph.isPageBreak() && contentRendered[0]) {
                builder.newPage();
            }
        } catch (Exception e) {
            log.warn("Failed to check paragraph-level page break: {}", e.getMessage());
        }

        // Set alignment for this paragraph
        builder.setAlignment(mapAlignment(paragraph.getAlignment()));

        // Task 4.10: List detection
        String listPrefix = getListPrefix(paragraph, listCounters);
        String indentation = getListIndentation(paragraph);

        boolean hasContent = false;

        // If this is a list item, prepend the list prefix as a formatted text segment
        if (!listPrefix.isEmpty()) {
            builder.addFormattedText(indentation + listPrefix, false, false, 0);
            hasContent = true;
        }

        // Task 4.7: Iterate XWPFRun objects for formatting extraction
        for (XWPFRun run : paragraph.getRuns()) {
            // Check for run-level page break
            try {
                if (isRunPageBreak(run) && contentRendered[0]) {
                    builder.newPage();
                }
            } catch (Exception e) {
                log.warn("Failed to check run-level page break: {}", e.getMessage());
            }

            // Task 4.8: Check for embedded images
            try {
                for (var picture : run.getEmbeddedPictures()) {
                    XWPFPictureData pictureData = picture.getPictureData();
                    if (pictureData != null) {
                        byte[] imageData = pictureData.getData();
                        if (imageData != null && imageData.length > 0) {
                            builder.addImage(imageData);
                            contentRendered[0] = true;
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to extract embedded image from paragraph: {}", e.getMessage());
            }

            // Extract text and formatting from each run
            String text = run.text();
            if (text != null && !text.isEmpty()) {
                boolean bold = run.isBold();
                boolean italic = run.isItalic();
                Double fontSizeDouble = run.getFontSizeAsDouble();
                float fontSize = (fontSizeDouble != null && fontSizeDouble > 0)
                        ? fontSizeDouble.floatValue()
                        : 0; // 0 signals default size to the builder

                int[] rgb = parseColor(run.getColor());
                builder.addFormattedText(text, bold, italic, fontSize, rgb[0], rgb[1], rgb[2]);
                hasContent = true;
                contentRendered[0] = true;
            }
        }

        // Always call endParagraph to flush accumulated text
        if (hasContent) {
            builder.endParagraph();
        }
    }

    /**
     * Determines the list prefix for a paragraph based on its numbering format.
     *
     * @return the prefix string ("• " for bullets, "N. " for numbered), or empty string
     */
    private String getListPrefix(XWPFParagraph paragraph, Map<String, Integer> listCounters) {
        String numFmt = paragraph.getNumFmt();
        if (numFmt == null) {
            return "";
        }

        if ("bullet".equalsIgnoreCase(numFmt)) {
            return "• ";
        }

        if ("decimal".equalsIgnoreCase(numFmt)) {
            String numId = getNumId(paragraph);
            int counter = listCounters.getOrDefault(numId, 0) + 1;
            listCounters.put(numId, counter);
            return counter + ". ";
        }

        return "";
    }

    /**
     * Returns indentation spaces for list items.
     */
    private String getListIndentation(XWPFParagraph paragraph) {
        String numFmt = paragraph.getNumFmt();
        if (numFmt == null) {
            return "";
        }
        // Apply indentation for list items
        return "  ";
    }

    /**
     * Extracts the numbering ID from a paragraph, used as the key for list counters.
     */
    private String getNumId(XWPFParagraph paragraph) {
        try {
            var numID = paragraph.getNumID();
            return numID != null ? numID.toString() : "default";
        } catch (Exception e) {
            return "default";
        }
    }

    /**
     * Maps a POI ParagraphAlignment to the internal TextAlignment enum.
     * BOTH (justify) maps to LEFT as a reasonable approximation.
     */
    private TextAlignment mapAlignment(ParagraphAlignment poiAlignment) {
        if (poiAlignment == null) {
            return TextAlignment.LEFT;
        }
        return switch (poiAlignment) {
            case CENTER -> TextAlignment.CENTER;
            case RIGHT -> TextAlignment.RIGHT;
            default -> TextAlignment.LEFT; // LEFT, BOTH, and others all map to LEFT
        };
    }

    /**
     * Checks whether a run contains a page break by inspecting the underlying XML break elements.
     * A run has a page break if any of its CTBr elements has type PAGE.
     *
     * @param run the XWPFRun to check
     * @return true if the run contains a page break
     */
    private boolean isRunPageBreak(XWPFRun run) {
        for (CTBr br : run.getCTR().getBrList()) {
            if (br.getType() == STBrType.PAGE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parses a 6-character hexadecimal RGB color string into an int array of {r, g, b}.
     * Returns {0, 0, 0} (black) for null, wrong-length, or non-hex inputs.
     * Package-private for testing.
     *
     * @param hexColor the hex color string (e.g., "FF0000" for red)
     * @return int array with {r, g, b} components (0-255 each)
     */
    int[] parseColor(String hexColor) {
        if (hexColor == null || hexColor.length() != 6) {
            return new int[]{0, 0, 0};
        }
        try {
            int r = Integer.parseInt(hexColor.substring(0, 2), 16);
            int g = Integer.parseInt(hexColor.substring(2, 4), 16);
            int b = Integer.parseInt(hexColor.substring(4, 6), 16);
            return new int[]{r, g, b};
        } catch (NumberFormatException e) {
            log.warn("Invalid color hex string '{}', defaulting to black", hexColor);
            return new int[]{0, 0, 0};
        }
    }

    /**
     * Processes headers from the DOCX document.
     */
    private void processHeaders(XWPFDocument docxDocument, PdfDocumentBuilder builder) {
        try {
            for (XWPFHeader header : docxDocument.getHeaderList()) {
                String text = header.getText();
                if (text != null && !text.trim().isEmpty()) {
                    builder.addHeaderText(text.trim());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract headers: {}", e.getMessage());
        }
    }

    /**
     * Processes footers from the DOCX document.
     */
    private void processFooters(XWPFDocument docxDocument, PdfDocumentBuilder builder) {
        try {
            for (XWPFFooter footer : docxDocument.getFooterList()) {
                String text = footer.getText();
                if (text != null && !text.trim().isEmpty()) {
                    builder.addFooterText(text.trim());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract footers: {}", e.getMessage());
        }
    }

    private void processTable(XWPFTable table, PdfDocumentBuilder builder) throws IOException {
        if (table.getRows().isEmpty()) {
            return;
        }

        // Determine table dimensions
        int numCols = table.getRow(0).getTableCells().size();
        int numRows = table.getRows().size();

        String[][] tableData = new String[numRows][numCols];

        // Extract table data
        for (int i = 0; i < numRows; i++) {
            XWPFTableRow row = table.getRow(i);
            for (int j = 0; j < numCols && j < row.getTableCells().size(); j++) {
                XWPFTableCell cell = row.getCell(j);
                tableData[i][j] = cell.getText();
            }
        }

        builder.addTable(tableData);
    }
}
