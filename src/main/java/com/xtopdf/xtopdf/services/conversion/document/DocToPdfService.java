package com.xtopdf.xtopdf.services.conversion.document;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Table;
import org.apache.poi.hwpf.usermodel.TableCell;
import org.apache.poi.hwpf.usermodel.TableRow;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
@Slf4j
public class DocToPdfService {

    private final PdfBackendProvider pdfBackend;

    public DocToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }

    public void convertDocToPdf(MultipartFile docFile, File pdfFile) throws IOException {
        try (var is = docFile.getInputStream();
             HWPFDocument doc = new HWPFDocument(is);
             PdfDocumentBuilder builder = pdfBackend.createBuilder()) {

            Range range = doc.getRange();
            int i = 0;
            while (i < range.numParagraphs()) {
                Paragraph para = range.getParagraph(i);

                if (para.isInTable()) {
                    i = renderTable(builder, range, i, doc);
                    continue;
                }

                float fontSize = detectHeadingSize(para, doc.getStyleSheet());
                renderParagraphRuns(builder, para, fontSize);
                i++;
            }
            builder.save(pdfFile);
        }
    }

    public void renderParagraphRuns(PdfDocumentBuilder builder, Paragraph para,
                             float fontSize) throws IOException {
        for (int r = 0; r < para.numCharacterRuns(); r++) {
            CharacterRun run = para.getCharacterRun(r);
            String text = cleanText(run.text());
            if (text.isEmpty()) {
                continue;
            }
            builder.addFormattedText(text, run.isBold(), run.isItalic(), fontSize);
        }
        builder.endParagraph();
    }

    public float detectHeadingSize(Paragraph para, org.apache.poi.hwpf.model.StyleSheet styles) {
        try {
            int styleIndex = para.getStyleIndex();
            var styleDesc = styles.getStyleDescription(styleIndex);
            if (styleDesc == null) {
                return 12f;
            }
            String styleName = styleDesc.getName();
            if (styleName == null) {
                return 12f;
            }
            return switch (styleName.toLowerCase()) {
                case "heading 1" -> 24f;
                case "heading 2" -> 20f;
                case "heading 3" -> 16f;
                case "heading 4" -> 14f;
                case "heading 5" -> 13f;
                case "heading 6" -> 12f;
                default -> 12f;
            };
        } catch (Exception e) {
            log.debug("Could not determine heading style, defaulting to 12pt");
            return 12f;
        }
    }

    /**
     * Provides the heading font size for a given level (1-6).
     * Used by both DOC and ODT converters.
     */
    public static float detectHeadingSize(int level) {
        return switch (level) {
            case 1 -> 24f;
            case 2 -> 20f;
            case 3 -> 16f;
            case 4 -> 14f;
            case 5 -> 13f;
            case 6 -> 12f;
            default -> 12f;
        };
    }

    private int renderTable(PdfDocumentBuilder builder, Range range, int startIdx,
                            HWPFDocument doc) throws IOException {
        try {
            Table table = range.getTable(range.getParagraph(startIdx));
            int numRows = table.numRows();
            int maxCols = 0;
            for (int r = 0; r < numRows; r++) {
                maxCols = Math.max(maxCols, table.getRow(r).numCells());
            }
            String[][] data = new String[numRows][maxCols];
            for (int r = 0; r < numRows; r++) {
                TableRow row = table.getRow(r);
                for (int c = 0; c < row.numCells(); c++) {
                    TableCell cell = row.getCell(c);
                    String text = cleanText(cell.text());
                    data[r][c] = text;
                }
            }
            builder.addTable(data);
            return startIdx + countTableParagraphs(table);
        } catch (Exception e) {
            log.debug("Table rendering failed, skipping table paragraph");
            return startIdx + 1;
        }
    }

    private int countTableParagraphs(Table table) {
        int count = 0;
        for (int r = 0; r < table.numRows(); r++) {
            TableRow row = table.getRow(r);
            for (int c = 0; c < row.numCells(); c++) {
                count += row.getCell(c).numParagraphs();
            }
        }
        return count;
    }

    /**
     * Cleans text by removing special characters that POI may include
     * (field codes, control characters).
     */
    private String cleanText(String text) {
        if (text == null) {
            return "";
        }
        // Remove control characters (except newline/tab) and field codes
        return text.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x07]", "").trim();
    }
}
