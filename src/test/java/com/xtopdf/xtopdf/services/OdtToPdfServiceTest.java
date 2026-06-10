package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import com.xtopdf.xtopdf.services.conversion.document.DocToPdfService;
import com.xtopdf.xtopdf.services.conversion.document.OdtToPdfService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;
import java.util.Arrays;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class OdtToPdfServiceTest {

    @Mock
    private PdfBackendProvider pdfBackend;

    @Mock
    private PdfDocumentBuilder builder;

    private OdtToPdfService odtToPdfService;

    @BeforeEach
    void setUp() {
        odtToPdfService = new OdtToPdfService(pdfBackend);
    }

    // ==========================================
    // Integration tests (real ODT files)
    // ==========================================

    @Nested
    class IntegrationTests {

        private OdtToPdfService realService;

        @BeforeEach
        void setUp() {
            var realBackend = new com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend();
            realService = new OdtToPdfService(realBackend);
        }

        @Test
        void convertOdtToPdf_ValidFile_Success(@TempDir Path tempDir) throws Exception {
            ClassPathResource resource = new ClassPathResource("test-files/test.odt");
            byte[] odtData = Files.readAllBytes(resource.getFile().toPath());

            MockMultipartFile odtFile = new MockMultipartFile(
                    "file", "test.odt",
                    "application/vnd.oasis.opendocument.text", odtData);

            File pdfFile = tempDir.resolve("output.pdf").toFile();

            realService.convertOdtToPdf(odtFile, pdfFile);

            assertThat(pdfFile).exists();
            assertThat(pdfFile.length()).isGreaterThan(0);
        }

        @Test
        void convertOdtToPdf_InvalidFormat_ThrowsIOException(@TempDir Path tempDir) {
            byte[] invalidOdtData = "Not a valid ODT file".getBytes();
            MockMultipartFile odtFile = new MockMultipartFile(
                    "file", "test.odt",
                    "application/vnd.oasis.opendocument.text", invalidOdtData);

            File pdfFile = tempDir.resolve("testOdtOutput.pdf").toFile();

            assertThatThrownBy(() -> realService.convertOdtToPdf(odtFile, pdfFile))
                    .isInstanceOf(IOException.class);
        }

        @Test
        void convertOdtToPdf_EmptyFile_ThrowsIOException(@TempDir Path tempDir) {
            MockMultipartFile odtFile = new MockMultipartFile(
                    "file", "empty.odt",
                    "application/vnd.oasis.opendocument.text", new byte[0]);

            File pdfFile = tempDir.resolve("output.pdf").toFile();

            assertThatThrownBy(() -> realService.convertOdtToPdf(odtFile, pdfFile))
                    .isInstanceOf(IOException.class);
        }

        @Test
        void convertOdtToPdf_FormattedDocument_Success(@TempDir Path tempDir) throws Exception {
            ClassPathResource resource = new ClassPathResource("test-files/test.odt");
            byte[] odtData = Files.readAllBytes(resource.getFile().toPath());

            MockMultipartFile odtFile = new MockMultipartFile(
                    "file", "formatted.odt",
                    "application/vnd.oasis.opendocument.text", odtData);

            File pdfFile = tempDir.resolve("formatted_output.pdf").toFile();

            realService.convertOdtToPdf(odtFile, pdfFile);

            assertThat(pdfFile).exists();
            assertThat(pdfFile.length()).isGreaterThan(0);
        }
    }

    // ==========================================
    // Unit tests — paragraph extraction
    // ==========================================

    @Nested
    class ParagraphRendering {

        @Test
        void paragraphNodeRendersTextAndCallsEndParagraph() throws IOException {
            Node paragraphNode = mockElement("text:p");
            Node textNode = mockTextNode("Hello World");
            NodeList children = mockNodeList(textNode);
            when(paragraphNode.getChildNodes()).thenReturn(children);

            odtToPdfService.renderParagraph(builder, paragraphNode, 12f);

            verify(builder).addFormattedText("Hello World", false, false, 12f);
            verify(builder).endParagraph();
        }

        @Test
        void emptyParagraphNodeCallsEndParagraphOnly() throws IOException {
            Node paragraphNode = mockElement("text:p");
            NodeList children = mockNodeList();
            when(paragraphNode.getChildNodes()).thenReturn(children);

            odtToPdfService.renderParagraph(builder, paragraphNode, 12f);

            verify(builder, never()).addFormattedText(anyString(), anyBoolean(), anyBoolean(), anyFloat());
            verify(builder).endParagraph();
        }

        @Test
        void paragraphWithMultipleTextNodesRendersAll() throws IOException {
            Node paragraphNode = mockElement("text:p");
            Node text1 = mockTextNode("First ");
            Node text2 = mockTextNode("Second");
            NodeList children = mockNodeList(text1, text2);
            when(paragraphNode.getChildNodes()).thenReturn(children);

            odtToPdfService.renderParagraph(builder, paragraphNode, 12f);

            var inOrder = inOrder(builder);
            inOrder.verify(builder).addFormattedText("First ", false, false, 12f);
            inOrder.verify(builder).addFormattedText("Second", false, false, 12f);
            inOrder.verify(builder).endParagraph();
        }
    }

    // ==========================================
    // Unit tests — heading style detection
    // ==========================================

    @Nested
    class HeadingRendering {

        @Test
        void headingLevel1RendersAtFontSize24() throws IOException {
            Element headingNode = mockElementWithAttribute("text:h", "text:outline-level", "1");
            Node textNode = mockTextNode("Heading 1");
            NodeList children = mockNodeList(textNode);
            when(headingNode.getChildNodes()).thenReturn(children);

            odtToPdfService.renderHeading(builder, headingNode);

            verify(builder).addFormattedText("Heading 1", true, false, 24f);
            verify(builder).endParagraph();
        }

        @Test
        void headingLevel2RendersAtFontSize20() throws IOException {
            Element headingNode = mockElementWithAttribute("text:h", "text:outline-level", "2");
            Node textNode = mockTextNode("Heading 2");
            NodeList children = mockNodeList(textNode);
            when(headingNode.getChildNodes()).thenReturn(children);

            odtToPdfService.renderHeading(builder, headingNode);

            verify(builder).addFormattedText("Heading 2", true, false, 20f);
            verify(builder).endParagraph();
        }

        @Test
        void headingLevel3RendersAtFontSize16() throws IOException {
            Element headingNode = mockElementWithAttribute("text:h", "text:outline-level", "3");
            Node textNode = mockTextNode("Heading 3");
            NodeList children = mockNodeList(textNode);
            when(headingNode.getChildNodes()).thenReturn(children);

            odtToPdfService.renderHeading(builder, headingNode);

            verify(builder).addFormattedText("Heading 3", true, false, 16f);
            verify(builder).endParagraph();
        }

        @Test
        void headingLevel4RendersAtFontSize14() throws IOException {
            Element headingNode = mockElementWithAttribute("text:h", "text:outline-level", "4");
            Node textNode = mockTextNode("H4");
            NodeList children = mockNodeList(textNode);
            when(headingNode.getChildNodes()).thenReturn(children);

            odtToPdfService.renderHeading(builder, headingNode);

            verify(builder).addFormattedText("H4", true, false, 14f);
            verify(builder).endParagraph();
        }

        @Test
        void headingLevel5RendersAtFontSize13() throws IOException {
            Element headingNode = mockElementWithAttribute("text:h", "text:outline-level", "5");
            Node textNode = mockTextNode("H5");
            NodeList children = mockNodeList(textNode);
            when(headingNode.getChildNodes()).thenReturn(children);

            odtToPdfService.renderHeading(builder, headingNode);

            verify(builder).addFormattedText("H5", true, false, 13f);
            verify(builder).endParagraph();
        }

        @Test
        void headingLevel6RendersAtFontSize12() throws IOException {
            Element headingNode = mockElementWithAttribute("text:h", "text:outline-level", "6");
            Node textNode = mockTextNode("H6");
            NodeList children = mockNodeList(textNode);
            when(headingNode.getChildNodes()).thenReturn(children);

            odtToPdfService.renderHeading(builder, headingNode);

            verify(builder).addFormattedText("H6", true, false, 12f);
            verify(builder).endParagraph();
        }

        @Test
        void headingWithMissingOutlineLevelDefaultsToLevel1() throws IOException {
            Element headingNode = mockElementWithAttribute("text:h", "text:outline-level", "");
            Node textNode = mockTextNode("Default heading");
            NodeList children = mockNodeList(textNode);
            when(headingNode.getChildNodes()).thenReturn(children);

            odtToPdfService.renderHeading(builder, headingNode);

            verify(builder).addFormattedText("Default heading", true, false, 24f);
            verify(builder).endParagraph();
        }

        @Test
        void headingWithInvalidOutlineLevelDefaultsToLevel1() throws IOException {
            Element headingNode = mockElementWithAttribute("text:h", "text:outline-level", "abc");
            Node textNode = mockTextNode("Invalid level");
            NodeList children = mockNodeList(textNode);
            when(headingNode.getChildNodes()).thenReturn(children);

            odtToPdfService.renderHeading(builder, headingNode);

            verify(builder).addFormattedText("Invalid level", true, false, 24f);
            verify(builder).endParagraph();
        }

        @Test
        void headingFontSizesMappedViaSameLogicAsDocToPdfService() {
            assertThat(DocToPdfService.detectHeadingSize(1)).isEqualTo(24f);
            assertThat(DocToPdfService.detectHeadingSize(2)).isEqualTo(20f);
            assertThat(DocToPdfService.detectHeadingSize(3)).isEqualTo(16f);
            assertThat(DocToPdfService.detectHeadingSize(4)).isEqualTo(14f);
            assertThat(DocToPdfService.detectHeadingSize(5)).isEqualTo(13f);
            assertThat(DocToPdfService.detectHeadingSize(6)).isEqualTo(12f);
        }
    }

    // ==========================================
    // Unit tests — list rendering
    // ==========================================

    @Nested
    class ListRendering {

        @Test
        void listWithSingleItemRendersWithBulletPrefix() throws IOException {
            Node listNode = mockElement("text:list");
            Node listItem = mockElement("text:list-item");
            Node paragraph = mockElement("text:p");
            Node textNode = mockTextNode("Item one");

            NodeList listChildren = mockNodeList(listItem);
            NodeList itemChildren = mockNodeList(paragraph);
            NodeList paraChildren = mockNodeList(textNode);

            when(listNode.getChildNodes()).thenReturn(listChildren);
            when(listItem.getChildNodes()).thenReturn(itemChildren);
            when(paragraph.getChildNodes()).thenReturn(paraChildren);
            when(paragraph.getNodeName()).thenReturn("text:p");

            odtToPdfService.renderList(builder, listNode, 0);

            var inOrder = inOrder(builder);
            inOrder.verify(builder).addFormattedText("\u2022 ", false, false, 12f);
            inOrder.verify(builder).addFormattedText("Item one", false, false, 12f);
            inOrder.verify(builder).endParagraph();
        }

        @Test
        void listWithMultipleItemsRendersEachWithBullet() throws IOException {
            Node listNode = mockElement("text:list");
            Node listItem1 = mockElement("text:list-item");
            Node listItem2 = mockElement("text:list-item");

            Node para1 = mockElement("text:p");
            Node text1 = mockTextNode("First");
            Node para2 = mockElement("text:p");
            Node text2 = mockTextNode("Second");

            NodeList listChildren = mockNodeList(listItem1, listItem2);
            NodeList item1Children = mockNodeList(para1);
            NodeList item2Children = mockNodeList(para2);
            NodeList para1Children = mockNodeList(text1);
            NodeList para2Children = mockNodeList(text2);

            when(listNode.getChildNodes()).thenReturn(listChildren);
            when(listItem1.getChildNodes()).thenReturn(item1Children);
            when(listItem2.getChildNodes()).thenReturn(item2Children);
            when(para1.getChildNodes()).thenReturn(para1Children);
            when(para2.getChildNodes()).thenReturn(para2Children);
            when(para1.getNodeName()).thenReturn("text:p");
            when(para2.getNodeName()).thenReturn("text:p");

            odtToPdfService.renderList(builder, listNode, 0);

            verify(builder, times(2)).addFormattedText("\u2022 ", false, false, 12f);
            verify(builder).addFormattedText("First", false, false, 12f);
            verify(builder).addFormattedText("Second", false, false, 12f);
            verify(builder, times(2)).endParagraph();
        }

        @Test
        void nestedListRendersWithIndentation() throws IOException {
            Node listNode = mockElement("text:list");
            Node listItem = mockElement("text:list-item");
            Node paragraph = mockElement("text:p");
            Node textNode = mockTextNode("Nested item");

            NodeList listChildren = mockNodeList(listItem);
            NodeList itemChildren = mockNodeList(paragraph);
            NodeList paraChildren = mockNodeList(textNode);

            when(listNode.getChildNodes()).thenReturn(listChildren);
            when(listItem.getChildNodes()).thenReturn(itemChildren);
            when(paragraph.getChildNodes()).thenReturn(paraChildren);
            when(paragraph.getNodeName()).thenReturn("text:p");

            odtToPdfService.renderList(builder, listNode, 2);

            verify(builder).addFormattedText("    \u2022 ", false, false, 12f);
        }
    }

    // ==========================================
    // Unit tests — table rendering
    // ==========================================

    @Nested
    class TableRendering {

        @Test
        void tableWithSingleCellRendersCorrectly() throws IOException {
            Node tableNode = mockElement("table:table");
            Node row = mockElement("table:table-row");
            Node cell = mockElement("table:table-cell");

            NodeList tableChildren = mockNodeList(row);
            NodeList rowChildren = mockNodeList(cell);

            when(tableNode.getChildNodes()).thenReturn(tableChildren);
            when(row.getChildNodes()).thenReturn(rowChildren);
            when(cell.getTextContent()).thenReturn("Cell data");

            odtToPdfService.renderTable(builder, tableNode);

            ArgumentCaptor<String[][]> captor1 = ArgumentCaptor.forClass(String[][].class);
            verify(builder).addTable(captor1.capture());
            assertThat(Arrays.deepEquals(captor1.getValue(), new String[][]{{"Cell data"}})).isTrue();
        }

        @Test
        void tableWithMultipleRowsAndColumnsRendersCorrectly() throws IOException {
            Node tableNode = mockElement("table:table");
            Node row1 = mockElement("table:table-row");
            Node row2 = mockElement("table:table-row");
            Node cell1a = mockElement("table:table-cell");
            Node cell1b = mockElement("table:table-cell");
            Node cell2a = mockElement("table:table-cell");
            Node cell2b = mockElement("table:table-cell");

            NodeList tableChildren = mockNodeList(row1, row2);
            NodeList row1Children = mockNodeList(cell1a, cell1b);
            NodeList row2Children = mockNodeList(cell2a, cell2b);

            when(tableNode.getChildNodes()).thenReturn(tableChildren);
            when(row1.getChildNodes()).thenReturn(row1Children);
            when(row2.getChildNodes()).thenReturn(row2Children);
            when(cell1a.getTextContent()).thenReturn("A1");
            when(cell1b.getTextContent()).thenReturn("B1");
            when(cell2a.getTextContent()).thenReturn("A2");
            when(cell2b.getTextContent()).thenReturn("B2");

            odtToPdfService.renderTable(builder, tableNode);

            ArgumentCaptor<String[][]> captor2 = ArgumentCaptor.forClass(String[][].class);
            verify(builder).addTable(captor2.capture());
            assertThat(Arrays.deepEquals(captor2.getValue(), new String[][]{{"A1", "B1"}, {"A2", "B2"}})).isTrue();
        }

        @Test
        void tableWithUnevenColumnsIsPadded() throws IOException {
            Node tableNode = mockElement("table:table");
            Node row1 = mockElement("table:table-row");
            Node row2 = mockElement("table:table-row");
            Node cell1a = mockElement("table:table-cell");
            Node cell1b = mockElement("table:table-cell");
            Node cell2a = mockElement("table:table-cell");

            NodeList tableChildren = mockNodeList(row1, row2);
            NodeList row1Children = mockNodeList(cell1a, cell1b);
            NodeList row2Children = mockNodeList(cell2a);

            when(tableNode.getChildNodes()).thenReturn(tableChildren);
            when(row1.getChildNodes()).thenReturn(row1Children);
            when(row2.getChildNodes()).thenReturn(row2Children);
            when(cell1a.getTextContent()).thenReturn("A1");
            when(cell1b.getTextContent()).thenReturn("B1");
            when(cell2a.getTextContent()).thenReturn("A2");

            odtToPdfService.renderTable(builder, tableNode);

            ArgumentCaptor<String[][]> captor3 = ArgumentCaptor.forClass(String[][].class);
            verify(builder).addTable(captor3.capture());
            assertThat(Arrays.deepEquals(captor3.getValue(), new String[][]{{"A1", "B1"}, {"A2", ""}})).isTrue();
        }

        @Test
        void emptyTableDoesNotCallAddTable() throws IOException {
            Node tableNode = mockElement("table:table");
            NodeList tableChildren = mockNodeList();
            when(tableNode.getChildNodes()).thenReturn(tableChildren);

            odtToPdfService.renderTable(builder, tableNode);

            verify(builder, never()).addTable(any(String[][].class));
        }
    }

    // ==========================================
    // Unit tests — inline spans (bold/italic)
    // ==========================================

    @Nested
    class InlineSpanRendering {

        @Test
        void spanWithDirectBoldAttributeRendersBold() throws IOException {
            Node paragraphNode = mockElement("text:p");
            Element spanNode = mockSpanWithDirectStyle("bold", null);
            when(spanNode.getTextContent()).thenReturn("Bold text");

            NodeList children = mockNodeList(spanNode);
            when(paragraphNode.getChildNodes()).thenReturn(children);

            odtToPdfService.renderParagraph(builder, paragraphNode, 12f);

            verify(builder).addFormattedText("Bold text", true, false, 12f);
            verify(builder).endParagraph();
        }

        @Test
        void spanWithDirectItalicAttributeRendersItalic() throws IOException {
            Node paragraphNode = mockElement("text:p");
            Element spanNode = mockSpanWithDirectStyle(null, "italic");
            when(spanNode.getTextContent()).thenReturn("Italic text");

            NodeList children = mockNodeList(spanNode);
            when(paragraphNode.getChildNodes()).thenReturn(children);

            odtToPdfService.renderParagraph(builder, paragraphNode, 12f);

            verify(builder).addFormattedText("Italic text", false, true, 12f);
            verify(builder).endParagraph();
        }

        @Test
        void spanWithBoldAndItalicRendersBothStyles() throws IOException {
            Node paragraphNode = mockElement("text:p");
            Element spanNode = mockSpanWithDirectStyle("bold", "italic");
            when(spanNode.getTextContent()).thenReturn("Bold italic");

            NodeList children = mockNodeList(spanNode);
            when(paragraphNode.getChildNodes()).thenReturn(children);

            odtToPdfService.renderParagraph(builder, paragraphNode, 12f);

            verify(builder).addFormattedText("Bold italic", true, true, 12f);
            verify(builder).endParagraph();
        }

        @Test
        void spanWithNoStyleRendersPlainText() throws IOException {
            Node paragraphNode = mockElement("text:p");
            Element spanNode = mockSpanWithDirectStyle(null, null);
            when(spanNode.getTextContent()).thenReturn("Plain span");

            NodeList children = mockNodeList(spanNode);
            when(paragraphNode.getChildNodes()).thenReturn(children);

            odtToPdfService.renderParagraph(builder, paragraphNode, 12f);

            verify(builder).addFormattedText("Plain span", false, false, 12f);
            verify(builder).endParagraph();
        }

        @Test
        void headingWithBoldSpanRendersBold() throws IOException {
            Element headingNode = mockElementWithAttribute("text:h", "text:outline-level", "2");
            Element spanNode = mockSpanWithDirectStyle("bold", null);
            when(spanNode.getTextContent()).thenReturn("Bold heading");

            NodeList children = mockNodeList(spanNode);
            when(headingNode.getChildNodes()).thenReturn(children);

            odtToPdfService.renderHeading(builder, headingNode);

            verify(builder).addFormattedText("Bold heading", true, false, 20f);
            verify(builder).endParagraph();
        }

        @Test
        void headingTextNodeRendersBoldByDefault() throws IOException {
            Element headingNode = mockElementWithAttribute("text:h", "text:outline-level", "1");
            Node textNode = mockTextNode("Heading text");

            NodeList children = mockNodeList(textNode);
            when(headingNode.getChildNodes()).thenReturn(children);

            odtToPdfService.renderHeading(builder, headingNode);

            verify(builder).addFormattedText("Heading text", true, false, 24f);
            verify(builder).endParagraph();
        }
    }

    // ==========================================
    // Unit tests — processOdtNode dispatch
    // ==========================================

    @Nested
    class NodeDispatch {

        @Test
        void processOdtNodeDispatchesParagraph() throws IOException {
            Node paragraphNode = mockElement("text:p");
            NodeList children = mockNodeList();
            when(paragraphNode.getChildNodes()).thenReturn(children);

            odtToPdfService.processOdtNode(builder, paragraphNode, 0);

            verify(builder).endParagraph();
        }

        @Test
        void processOdtNodeDispatchesHeading() throws IOException {
            Element headingNode = mockElementWithAttribute("text:h", "text:outline-level", "1");
            NodeList children = mockNodeList();
            when(headingNode.getChildNodes()).thenReturn(children);

            odtToPdfService.processOdtNode(builder, headingNode, 0);

            verify(builder).endParagraph();
        }

        @Test
        void processOdtNodeSkipsUnknownNodes() throws IOException {
            Node unknownNode = mockElement("draw:frame");

            odtToPdfService.processOdtNode(builder, unknownNode, 0);

            verifyNoInteractions(builder);
        }

        @Test
        void processOdtNodeHandlesNull() throws IOException {
            odtToPdfService.processOdtNode(builder, null, 0);

            verifyNoInteractions(builder);
        }
    }

    // ==========================================
    // Unit tests — error handling
    // ==========================================

    @Nested
    class ErrorHandling {

        @Test
        void nullMultipartFileThrowsIOException(@TempDir Path tempDir) {
            File pdfFile = tempDir.resolve("nullInput.pdf").toFile();

            assertThatThrownBy(() -> odtToPdfService.convertOdtToPdf(null, pdfFile))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("null");
        }

        @Test
        void nullOutputFileThrowsIOException() {
            MockMultipartFile odtFile = new MockMultipartFile(
                    "file", "test.odt",
                    "application/vnd.oasis.opendocument.text", "data".getBytes());

            assertThatThrownBy(() -> odtToPdfService.convertOdtToPdf(odtFile, null))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("null");
        }
    }

    // ==========================================
    // Helper methods for mocking DOM nodes
    // ==========================================

    private Node mockElement(String nodeName) {
        Element element = mock(Element.class);
        when(element.getNodeName()).thenReturn(nodeName);
        when(element.getNodeType()).thenReturn(Node.ELEMENT_NODE);
        return element;
    }

    private Element mockElementWithAttribute(String nodeName, String attrName, String attrValue) {
        Element element = mock(Element.class);
        when(element.getNodeName()).thenReturn(nodeName);
        when(element.getNodeType()).thenReturn(Node.ELEMENT_NODE);
        when(element.getAttribute(attrName)).thenReturn(attrValue);
        return element;
    }

    private Node mockTextNode(String text) {
        Node textNode = mock(Node.class);
        when(textNode.getNodeType()).thenReturn(Node.TEXT_NODE);
        when(textNode.getTextContent()).thenReturn(text);
        when(textNode.getNodeName()).thenReturn("#text");
        return textNode;
    }

    private Element mockSpanWithDirectStyle(String fontWeight, String fontStyle) {
        Element spanNode = mock(Element.class);
        when(spanNode.getNodeName()).thenReturn("text:span");
        when(spanNode.getNodeType()).thenReturn(Node.ELEMENT_NODE);
        when(spanNode.getAttribute("fo:font-weight")).thenReturn(fontWeight != null ? fontWeight : "");
        when(spanNode.getAttribute("fo:font-style")).thenReturn(fontStyle != null ? fontStyle : "");
        lenient().when(spanNode.getAttribute("text:style-name")).thenReturn("");
        return spanNode;
    }

    private NodeList mockNodeList(Node... nodes) {
        return new NodeList() {
            @Override
            public Node item(int index) {
                return index < nodes.length ? nodes[index] : null;
            }

            @Override
            public int getLength() {
                return nodes.length;
            }
        };
    }
}
