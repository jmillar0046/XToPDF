package com.xtopdf.xtopdf.services.conversion.document;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import lombok.extern.slf4j.Slf4j;
import org.odftoolkit.odfdom.doc.OdfTextDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OdtToPdfService {

    private final PdfBackendProvider pdfBackend;

    public OdtToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }

    public void convertOdtToPdf(MultipartFile odtFile, File pdfFile) throws IOException {
        if (odtFile == null) {
            throw new IOException("ODT file must not be null");
        }
        if (pdfFile == null) {
            throw new IOException("Output PDF file must not be null");
        }

        try (var is = odtFile.getInputStream();
             PdfDocumentBuilder builder = pdfBackend.createBuilder()) {

            OdfTextDocument odt = OdfTextDocument.loadDocument(is);
            Element textBody = odt.getContentRoot();

            if (textBody == null) {
                builder.addParagraph("Document contains no text content.");
                builder.save(pdfFile);
                return;
            }

            NodeList children = textBody.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                processOdtNode(builder, children.item(i), 0);
            }
            builder.save(pdfFile);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error processing ODT file", e);
            throw new IOException("Error processing ODT file", e);
        }
    }

    private static final int MAX_NESTING_DEPTH = 20;

    void processOdtNode(PdfDocumentBuilder builder, Node node, int indentLevel) throws IOException {
        if (node == null || indentLevel > MAX_NESTING_DEPTH) {
            return;
        }
        String nodeName = node.getNodeName();
        if (nodeName == null) {
            return;
        }
        switch (nodeName) {
            case "text:p" -> renderParagraph(builder, node, 12f);
            case "text:h" -> renderHeading(builder, node);
            case "text:list" -> renderList(builder, node, indentLevel);
            case "table:table" -> renderTable(builder, node);
            default -> { /* skip non-content nodes */ }
        }
    }

    void renderHeading(PdfDocumentBuilder builder, Node node) throws IOException {
        int level = 1;
        if (node instanceof Element element) {
            String levelAttr = element.getAttribute("text:outline-level");
            if (levelAttr != null && !levelAttr.isEmpty()) {
                try {
                    level = Integer.parseInt(levelAttr);
                } catch (NumberFormatException e) {
                    level = 1;
                }
            }
        }
        float fontSize = DocToPdfService.detectHeadingSize(level);
        renderInlineContent(builder, node, fontSize, true);
    }

    void renderParagraph(PdfDocumentBuilder builder, Node node, float fontSize) throws IOException {
        renderInlineContent(builder, node, fontSize, false);
    }

    void renderInlineContent(PdfDocumentBuilder builder, Node node, float fontSize,
                                    boolean boldDefault) throws IOException {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE) {
                String text = child.getTextContent();
                if (text != null && !text.isEmpty()) {
                    builder.addFormattedText(text, boldDefault, false, fontSize);
                }
            } else if ("text:span".equals(child.getNodeName())) {
                boolean bold = isBold(child) || boldDefault;
                boolean italic = isItalic(child);
                String text = child.getTextContent();
                if (text != null && !text.isEmpty()) {
                    builder.addFormattedText(text, bold, italic, fontSize);
                }
            }
        }
        builder.endParagraph();
    }

    void renderList(PdfDocumentBuilder builder, Node listNode, int indentLevel) throws IOException {
        NodeList items = listNode.getChildNodes();
        for (int i = 0; i < items.getLength(); i++) {
            Node item = items.item(i);
            if ("text:list-item".equals(item.getNodeName())) {
                String prefix = "  ".repeat(indentLevel) + "\u2022 ";
                builder.addFormattedText(prefix, false, false, 12f);
                NodeList itemChildren = item.getChildNodes();
                for (int j = 0; j < itemChildren.getLength(); j++) {
                    processOdtNode(builder, itemChildren.item(j), indentLevel + 1);
                }
            }
        }
    }

    void renderTable(PdfDocumentBuilder builder, Node tableNode) throws IOException {
        NodeList tableChildren = tableNode.getChildNodes();
        List<String[]> rows = new ArrayList<>();
        int maxCols = 0;

        for (int i = 0; i < tableChildren.getLength(); i++) {
            Node child = tableChildren.item(i);
            if ("table:table-row".equals(child.getNodeName())) {
                NodeList cells = child.getChildNodes();
                List<String> rowData = new ArrayList<>();
                for (int j = 0; j < cells.getLength(); j++) {
                    Node cell = cells.item(j);
                    if ("table:table-cell".equals(cell.getNodeName())) {
                        String text = cell.getTextContent();
                        rowData.add(text != null ? text.trim() : "");
                    }
                }
                maxCols = Math.max(maxCols, rowData.size());
                rows.add(rowData.toArray(new String[0]));
            }
        }

        if (!rows.isEmpty()) {
            String[][] data = new String[rows.size()][maxCols];
            for (int r = 0; r < rows.size(); r++) {
                String[] row = rows.get(r);
                for (int c = 0; c < maxCols; c++) {
                    data[r][c] = c < row.length ? row[c] : "";
                }
            }
            builder.addTable(data);
        }
    }

    boolean isBold(Node spanNode) {
        if (spanNode instanceof Element element) {
            String fontWeight = element.getAttribute("fo:font-weight");
            if ("bold".equalsIgnoreCase(fontWeight)) {
                return true;
            }
            String styleName = element.getAttribute("text:style-name");
            if (styleName != null && !styleName.isEmpty()) {
                return checkStyleForBold(spanNode, styleName);
            }
        }
        return false;
    }

    boolean isItalic(Node spanNode) {
        if (spanNode instanceof Element element) {
            String fontStyle = element.getAttribute("fo:font-style");
            if ("italic".equalsIgnoreCase(fontStyle)) {
                return true;
            }
            String styleName = element.getAttribute("text:style-name");
            if (styleName != null && !styleName.isEmpty()) {
                return checkStyleForItalic(spanNode, styleName);
            }
        }
        return false;
    }

    private boolean checkStyleForBold(Node node, String styleName) {
        Node root = node.getOwnerDocument() != null ? node.getOwnerDocument().getDocumentElement() : null;
        if (root == null) {
            return false;
        }
        return findStyleProperty(root, styleName, "fo:font-weight", "bold");
    }

    private boolean checkStyleForItalic(Node node, String styleName) {
        Node root = node.getOwnerDocument() != null ? node.getOwnerDocument().getDocumentElement() : null;
        if (root == null) {
            return false;
        }
        return findStyleProperty(root, styleName, "fo:font-style", "italic");
    }

    private boolean findStyleProperty(Node root, String styleName, String propertyName, String expectedValue) {
        NodeList allChildren = root.getChildNodes();
        for (int i = 0; i < allChildren.getLength(); i++) {
            Node child = allChildren.item(i);
            if ("office:automatic-styles".equals(child.getNodeName()) || "office:styles".equals(child.getNodeName())) {
                NodeList styles = child.getChildNodes();
                for (int j = 0; j < styles.getLength(); j++) {
                    Node styleNode = styles.item(j);
                    if (styleNode instanceof Element styleElement && "style:style".equals(styleNode.getNodeName())) {
                        if (styleName.equals(styleElement.getAttribute("style:name"))) {
                            return hasTextProperty(styleElement, propertyName, expectedValue);
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean hasTextProperty(Element styleElement, String propertyName, String expectedValue) {
        NodeList children = styleElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element propElement && "style:text-properties".equals(child.getNodeName())) {
                String value = propElement.getAttribute(propertyName);
                if (expectedValue.equalsIgnoreCase(value)) {
                    return true;
                }
            }
        }
        return false;
    }
}
