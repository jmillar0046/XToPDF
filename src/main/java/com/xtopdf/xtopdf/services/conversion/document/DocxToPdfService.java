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
import java.util.Objects;

/**
 * Converts DOCX files to PDF, preserving document order, text formatting,
 * images, headers/footers, lists, alignment, color, and page breaks.
 */
@Service
@Slf4j
public class DocxToPdfService {

    private static final String BULLET_PREFIX = "• ";
    private static final String LIST_INDENT = "  ";
    private static final String NUM_FMT_BULLET = "bullet";
    private static final String NUM_FMT_DECIMAL = "decimal";
    private static final String DEFAULT_NUM_ID = "default";

    private final PdfBackendProvider pdfBackend;

    public DocxToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }

    public void convertDocxToPdf(MultipartFile docxFile, File pdfFile) throws IOException {
        Objects.requireNonNull(docxFile, "docxFile must not be null");
        Objects.requireNonNull(pdfFile, "pdfFile must not be null");

        try (var fis = docxFile.getInputStream();
             XWPFDocument document = new XWPFDocument(fis);
             PdfDocumentBuilder builder = pdfBackend.createBuilder()) {

            var state = new ConversionState();

            renderHeaders(document, builder);
            renderBodyElements(document, builder, state);
            renderFooters(document, builder);

            builder.save(pdfFile);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error processing DOCX file: {}", e.getMessage(), e);
            throw new IOException("Error processing DOCX file: " + e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------
    // Top-level rendering
    // ------------------------------------------------------------------

    private void renderBodyElements(XWPFDocument document, PdfDocumentBuilder builder,
                                    ConversionState state) throws IOException {
        for (IBodyElement element : document.getBodyElements()) {
            if (element instanceof XWPFParagraph paragraph) {
                renderParagraph(paragraph, builder, state);
            } else if (element instanceof XWPFTable table) {
                renderTable(table, builder);
            }
        }
    }

    // ------------------------------------------------------------------
    // Paragraph rendering
    // ------------------------------------------------------------------

    private void renderParagraph(XWPFParagraph paragraph, PdfDocumentBuilder builder,
                                 ConversionState state) throws IOException {
        handleParagraphPageBreak(paragraph, builder, state);
        builder.setAlignment(mapAlignment(paragraph.getAlignment()));

        boolean hasContent = renderListPrefix(paragraph, builder, state);

        for (XWPFRun run : paragraph.getRuns()) {
            handleRunPageBreak(run, builder, state);
            renderEmbeddedImages(run, builder, state);
            hasContent |= renderRunText(run, builder, state);
        }

        if (hasContent) {
            builder.endParagraph();
        }
    }

    private boolean renderListPrefix(XWPFParagraph paragraph, PdfDocumentBuilder builder,
                                     ConversionState state) throws IOException {
        String prefix = resolveListPrefix(paragraph, state);
        if (prefix.isEmpty()) {
            return false;
        }
        builder.addFormattedText(LIST_INDENT + prefix, false, false, 0);
        return true;
    }

    private boolean renderRunText(XWPFRun run, PdfDocumentBuilder builder,
                                  ConversionState state) throws IOException {
        String text = run.text();
        if (text == null || text.isEmpty()) {
            return false;
        }

        float fontSize = extractFontSize(run);
        Color color = Color.fromHex(run.getColor());

        builder.addFormattedText(text, run.isBold(), run.isItalic(), fontSize,
                color.r(), color.g(), color.b());
        state.markContentRendered();
        return true;
    }

    // ------------------------------------------------------------------
    // Page break handling
    // ------------------------------------------------------------------

    private void handleParagraphPageBreak(XWPFParagraph paragraph, PdfDocumentBuilder builder,
                                          ConversionState state) {
        try {
            if (paragraph.isPageBreak() && state.hasContentBeenRendered()) {
                builder.newPage();
            }
        } catch (Exception e) {
            log.warn("Failed to check paragraph-level page break: {}", e.getMessage());
        }
    }

    private void handleRunPageBreak(XWPFRun run, PdfDocumentBuilder builder,
                                    ConversionState state) {
        try {
            if (hasPageBreak(run) && state.hasContentBeenRendered()) {
                builder.newPage();
            }
        } catch (Exception e) {
            log.warn("Failed to check run-level page break: {}", e.getMessage());
        }
    }

    private boolean hasPageBreak(XWPFRun run) {
        return run.getCTR().getBrList().stream()
                .anyMatch(br -> br.getType() == STBrType.PAGE);
    }

    // ------------------------------------------------------------------
    // Image extraction
    // ------------------------------------------------------------------

    private void renderEmbeddedImages(XWPFRun run, PdfDocumentBuilder builder,
                                      ConversionState state) {
        try {
            for (var picture : run.getEmbeddedPictures()) {
                var pictureData = picture.getPictureData();
                if (pictureData == null) continue;

                byte[] imageData = pictureData.getData();
                if (imageData != null && imageData.length > 0) {
                    builder.addImage(imageData);
                    state.markContentRendered();
                }
            }
        } catch (IOException e) {
            log.warn("Failed to render embedded image: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("Unexpected error extracting embedded image: {}", e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // List detection
    // ------------------------------------------------------------------

    private String resolveListPrefix(XWPFParagraph paragraph, ConversionState state) {
        String numFmt = paragraph.getNumFmt();
        if (numFmt == null) {
            return "";
        }
        if (NUM_FMT_BULLET.equalsIgnoreCase(numFmt)) {
            return BULLET_PREFIX;
        }
        if (NUM_FMT_DECIMAL.equalsIgnoreCase(numFmt)) {
            String numId = extractNumId(paragraph);
            return state.nextListNumber(numId) + ". ";
        }
        return "";
    }

    private String extractNumId(XWPFParagraph paragraph) {
        try {
            var numID = paragraph.getNumID();
            return numID != null ? numID.toString() : DEFAULT_NUM_ID;
        } catch (Exception e) {
            return DEFAULT_NUM_ID;
        }
    }

    // ------------------------------------------------------------------
    // Alignment mapping
    // ------------------------------------------------------------------

    private TextAlignment mapAlignment(ParagraphAlignment poiAlignment) {
        if (poiAlignment == null) {
            return TextAlignment.LEFT;
        }
        return switch (poiAlignment) {
            case CENTER -> TextAlignment.CENTER;
            case RIGHT -> TextAlignment.RIGHT;
            default -> TextAlignment.LEFT;
        };
    }

    // ------------------------------------------------------------------
    // Formatting extraction
    // ------------------------------------------------------------------

    private float extractFontSize(XWPFRun run) {
        Double size = run.getFontSizeAsDouble();
        return (size != null && size > 0) ? size.floatValue() : 0;
    }

    /**
     * Parses a hex color string. Delegates to {@link Color#fromHex(String)}.
     * Package-private for backward compatibility with existing tests.
     */
    int[] parseColor(String hexColor) {
        Color c = Color.fromHex(hexColor);
        return new int[]{c.r(), c.g(), c.b()};
    }

    // ------------------------------------------------------------------
    // Headers and footers
    // ------------------------------------------------------------------

    private void renderHeaders(XWPFDocument document, PdfDocumentBuilder builder) {
        try {
            for (XWPFHeader header : document.getHeaderList()) {
                String text = header.getText();
                if (text != null && !text.trim().isEmpty()) {
                    builder.addHeaderText(text.trim());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract headers: {}", e.getMessage());
        }
    }

    private void renderFooters(XWPFDocument document, PdfDocumentBuilder builder) {
        try {
            for (XWPFFooter footer : document.getFooterList()) {
                String text = footer.getText();
                if (text != null && !text.trim().isEmpty()) {
                    builder.addFooterText(text.trim());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract footers: {}", e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Table rendering
    // ------------------------------------------------------------------

    private void renderTable(XWPFTable table, PdfDocumentBuilder builder) throws IOException {
        if (table.getRows().isEmpty()) {
            return;
        }

        int numCols = table.getRow(0).getTableCells().size();
        int numRows = table.getRows().size();
        String[][] tableData = new String[numRows][numCols];

        for (int i = 0; i < numRows; i++) {
            XWPFTableRow row = table.getRow(i);
            for (int j = 0; j < numCols && j < row.getTableCells().size(); j++) {
                tableData[i][j] = row.getCell(j).getText();
            }
        }

        builder.addTable(tableData);
    }
}
