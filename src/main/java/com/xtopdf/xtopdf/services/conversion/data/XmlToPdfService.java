package com.xtopdf.xtopdf.services.conversion.data;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Service for converting XML files to PDF.
 * Uses JSoup's XML parser which is inherently XXE-safe (no entity resolution, no DTD processing).
 * Renders XML with proper indentation, monospace font, and optional syntax highlighting.
 */
@Slf4j
@Service
public class XmlToPdfService {

    private final PdfBackendProvider pdfBackend;

    public XmlToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }

    public void convertXmlToPdf(MultipartFile xmlFile, File pdfFile) throws IOException {
        if (xmlFile == null) {
            throw new IOException("Input file must not be null");
        }
        if (pdfFile == null) {
            throw new IOException("Output file must not be null");
        }

        String rawXml = new String(xmlFile.getBytes(), StandardCharsets.UTF_8);

        // JSoup XML parser: inherently XXE-safe (no entity resolution, no DTD processing)
        org.jsoup.nodes.Document doc = Jsoup.parse(rawXml, "", Parser.xmlParser());

        // Strip DOCTYPE declarations — they should not appear in PDF output
        // and may contain entity/URI declarations from malicious input
        var doctypeNodes = new ArrayList<Node>();
        for (Node child : doc.childNodes()) {
            if (child instanceof DocumentType) {
                doctypeNodes.add(child);
            }
        }
        doctypeNodes.forEach(Node::remove);

        doc.outputSettings()
                .indentAmount(2)
                .outline(false)
                .charset(StandardCharsets.UTF_8);

        String prettyXml = doc.html();

        try (PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
            for (String line : prettyXml.split("\n")) {
                renderHighlightedLine(builder, line);
                builder.endParagraph();
            }
            builder.save(pdfFile);
        }
    }

    /**
     * Renders a single line of XML with syntax highlighting using indexOf-based scanning.
     * Colors: tags (blue), attribute names (red), attribute values (green), text (black).
     * Uses indexOf-based loops — no regex on user content (ReDoS prevention).
     */
    private void renderHighlightedLine(PdfDocumentBuilder builder, String line) throws IOException {
        int pos = 0;
        int len = line.length();

        while (pos < len) {
            int tagStart = line.indexOf('<', pos);

            if (tagStart < 0) {
                // Remaining text content
                String text = line.substring(pos);
                if (!text.isEmpty()) {
                    builder.addFormattedText(text, false, false, 10f, 0, 0, 0);
                }
                break;
            }

            // Text content before the tag
            if (tagStart > pos) {
                String textBefore = line.substring(pos, tagStart);
                if (!textBefore.isEmpty()) {
                    builder.addFormattedText(textBefore, false, false, 10f, 0, 0, 0);
                }
            }

            int tagEnd = line.indexOf('>', tagStart);
            if (tagEnd < 0) {
                // Unclosed tag — render rest as text
                builder.addFormattedText(line.substring(tagStart), false, false, 10f, 0, 0, 0);
                break;
            }

            // We have a complete tag from tagStart to tagEnd (inclusive)
            String tagContent = line.substring(tagStart, tagEnd + 1);
            renderTag(builder, tagContent);
            pos = tagEnd + 1;
        }
    }

    /**
     * Renders a single XML tag with syntax highlighting.
     * Uses indexOf-based parsing — no regex on user content.
     */
    private void renderTag(PdfDocumentBuilder builder, String tag) throws IOException {
        // Check if this is a comment, processing instruction, or CDATA
        if (tag.startsWith("<!--") || tag.startsWith("<?") || tag.startsWith("<![CDATA[")) {
            builder.addFormattedText(tag, false, false, 10f, 0, 0, 180);
            return;
        }

        // Find end of tag name (first space or end of tag content)
        int contentStart = tag.startsWith("</") ? 2 : 1;
        int nameEnd = contentStart;
        int tagLen = tag.length();

        while (nameEnd < tagLen && tag.charAt(nameEnd) != ' '
                && tag.charAt(nameEnd) != '/' && tag.charAt(nameEnd) != '>') {
            nameEnd++;
        }

        // Render opening bracket + tag name in blue
        String openBracket = tag.substring(0, contentStart);
        String tagName = tag.substring(contentStart, nameEnd);
        builder.addFormattedText(openBracket + tagName, false, false, 10f, 0, 0, 180);

        // Parse attributes between nameEnd and the closing bracket
        int pos = nameEnd;
        int closingStart = findClosingBracket(tag);

        while (pos < closingStart) {
            char ch = tag.charAt(pos);

            if (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r') {
                builder.addFormattedText(String.valueOf(ch), false, false, 10f, 0, 0, 0);
                pos++;
                continue;
            }

            // Look for attribute name (ends at '=')
            int equalsPos = tag.indexOf('=', pos);
            if (equalsPos < 0 || equalsPos >= closingStart) {
                // No more attributes — render remaining as blue (could be '/' in self-closing)
                String remaining = tag.substring(pos, closingStart);
                if (!remaining.isBlank()) {
                    builder.addFormattedText(remaining, false, false, 10f, 0, 0, 180);
                }
                break;
            }

            // Attribute name in red
            String attrName = tag.substring(pos, equalsPos);
            builder.addFormattedText(attrName, false, false, 10f, 180, 0, 0);

            // Equals sign in black
            builder.addFormattedText("=", false, false, 10f, 0, 0, 0);
            pos = equalsPos + 1;

            // Attribute value in green (including quotes)
            if (pos < closingStart && (tag.charAt(pos) == '"' || tag.charAt(pos) == '\'')) {
                char quote = tag.charAt(pos);
                int valueEnd = tag.indexOf(quote, pos + 1);
                if (valueEnd < 0 || valueEnd >= closingStart) {
                    valueEnd = closingStart - 1;
                }
                String attrValue = tag.substring(pos, valueEnd + 1);
                builder.addFormattedText(attrValue, false, false, 10f, 0, 128, 0);
                pos = valueEnd + 1;
            } else {
                // Unquoted attribute value — render until next space
                int valueEnd = pos;
                while (valueEnd < closingStart && tag.charAt(valueEnd) != ' ') {
                    valueEnd++;
                }
                String attrValue = tag.substring(pos, valueEnd);
                builder.addFormattedText(attrValue, false, false, 10f, 0, 128, 0);
                pos = valueEnd;
            }
        }

        // Render closing bracket in blue
        String closingBracket = tag.substring(closingStart);
        builder.addFormattedText(closingBracket, false, false, 10f, 0, 0, 180);
    }

    /**
     * Finds the start of the closing bracket sequence (e.g., "/>", ">").
     */
    private int findClosingBracket(String tag) {
        int len = tag.length();
        if (len < 2) return len;

        // Check for self-closing "/>"
        if (tag.charAt(len - 2) == '/') {
            return len - 2;
        }
        // Regular closing ">"
        return len - 1;
    }
}
