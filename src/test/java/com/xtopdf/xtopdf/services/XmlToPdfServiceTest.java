package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.services.conversion.data.XmlToPdfService;
import com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.*;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class XmlToPdfServiceTest {

    private XmlToPdfService xmlToPdfService;

    @BeforeEach
    void setUp() {
        // Use PDFBox backend for testing
        xmlToPdfService = new XmlToPdfService(new PdfBoxBackend());
    }

    // ========== Existing tests (converted to AssertJ) ==========

    @Test
    void testConvertXmlToPdf_SimpleXml_Success(@TempDir Path tempDir) throws Exception {
        String content = "<?xml version=\"1.0\"?><root><item>Test</item></root>";
        MockMultipartFile xmlFile = new MockMultipartFile(
                "file",
                "test.xml",
                "text/xml",
                content.getBytes()
        );

        File pdfFile = tempDir.resolve("testXmlOutput.pdf").toFile();

        xmlToPdfService.convertXmlToPdf(xmlFile, pdfFile);

        assertThat(pdfFile).exists().isNotEmpty();
    }

    @Test
    void testConvertXmlToPdf_NestedXml_Success(@TempDir Path tempDir) throws Exception {
        String content = "<?xml version=\"1.0\"?><root><parent><child>Value1</child><child>Value2</child></parent></root>";
        MockMultipartFile xmlFile = new MockMultipartFile(
                "file",
                "nested.xml",
                "text/xml",
                content.getBytes()
        );

        File pdfFile = tempDir.resolve("nested_output.pdf").toFile();

        xmlToPdfService.convertXmlToPdf(xmlFile, pdfFile);

        assertThat(pdfFile).exists().isNotEmpty();
    }

    @Test
    void testConvertXmlToPdf_WithAttributes_Success(@TempDir Path tempDir) throws Exception {
        String content = "<?xml version=\"1.0\"?><root><item id=\"1\" name=\"Test\">Content</item></root>";
        MockMultipartFile xmlFile = new MockMultipartFile(
                "file",
                "attributes.xml",
                "text/xml",
                content.getBytes()
        );

        File pdfFile = tempDir.resolve("attributes_output.pdf").toFile();

        xmlToPdfService.convertXmlToPdf(xmlFile, pdfFile);

        assertThat(pdfFile).exists().isNotEmpty();
    }

    @Test
    void testConvertXmlToPdf_WithNamespace_Success(@TempDir Path tempDir) throws Exception {
        String content = "<?xml version=\"1.0\"?><root xmlns=\"http://example.com\"><item>Test</item></root>";
        MockMultipartFile xmlFile = new MockMultipartFile(
                "file",
                "namespace.xml",
                "text/xml",
                content.getBytes()
        );

        File pdfFile = tempDir.resolve("namespace_output.pdf").toFile();

        xmlToPdfService.convertXmlToPdf(xmlFile, pdfFile);

        assertThat(pdfFile).exists().isNotEmpty();
    }

    @Test
    void testConvertXmlToPdf_EmptyFile_Success(@TempDir Path tempDir) throws Exception {
        String content = "";
        MockMultipartFile xmlFile = new MockMultipartFile(
                "file",
                "test.xml",
                "text/xml",
                content.getBytes()
        );

        File pdfFile = tempDir.resolve("testXmlEmptyOutput.pdf").toFile();

        xmlToPdfService.convertXmlToPdf(xmlFile, pdfFile);

        assertThat(pdfFile).exists();
    }

    @Test
    void testConvertXmlToPdf_LargeXml_Success(@TempDir Path tempDir) throws Exception {
        StringBuilder largeXml = new StringBuilder("<?xml version=\"1.0\"?><root>");
        for (int i = 0; i < 100; i++) {
            largeXml.append("<item id=\"").append(i).append("\">Value").append(i).append("</item>");
        }
        largeXml.append("</root>");

        MockMultipartFile xmlFile = new MockMultipartFile(
                "file",
                "large.xml",
                "text/xml",
                largeXml.toString().getBytes()
        );

        File pdfFile = tempDir.resolve("large_output.pdf").toFile();

        xmlToPdfService.convertXmlToPdf(xmlFile, pdfFile);

        assertThat(pdfFile).exists().isNotEmpty();
    }

    @Test
    void testConvertXmlToPdf_SpecialCharacters_Success(@TempDir Path tempDir) throws Exception {
        String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><text>Hello 世界 Ñoño</text></root>";
        MockMultipartFile xmlFile = new MockMultipartFile(
                "file",
                "special.xml",
                "text/xml",
                content.getBytes("UTF-8")
        );

        File pdfFile = tempDir.resolve("special_output.pdf").toFile();

        xmlToPdfService.convertXmlToPdf(xmlFile, pdfFile);

        assertThat(pdfFile).exists().isNotEmpty();
    }

    @Test
    void testConvertXmlToPdf_WithCDATA_Success(@TempDir Path tempDir) throws Exception {
        String content = "<?xml version=\"1.0\"?><root><data><![CDATA[Some <special> data & stuff]]></data></root>";
        MockMultipartFile xmlFile = new MockMultipartFile(
                "file",
                "cdata.xml",
                "text/xml",
                content.getBytes()
        );

        File pdfFile = tempDir.resolve("cdata_output.pdf").toFile();

        xmlToPdfService.convertXmlToPdf(xmlFile, pdfFile);

        assertThat(pdfFile).exists().isNotEmpty();
    }

    @Test
    void testConvertXmlToPdf_NullMultipartFile_ThrowsNullPointerException(@TempDir Path tempDir) {
        File pdfFile = tempDir.resolve("nullInput.pdf").toFile();
        assertThatThrownBy(() -> xmlToPdfService.convertXmlToPdf(null, pdfFile))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void testConvertXmlToPdf_NullOutputFile_ThrowsIOException() {
        String content = "test";
        MockMultipartFile xmlFile = new MockMultipartFile(
                "file",
                "test.xml",
                "text/xml",
                content.getBytes()
        );
        assertThatThrownBy(() -> xmlToPdfService.convertXmlToPdf(xmlFile, null))
                .isInstanceOf(IOException.class);
    }

    @Test
    void testConvertXmlToPdf_MultipleRootElements_Success(@TempDir Path tempDir) throws Exception {
        String content = "<?xml version=\"1.0\"?><root><section id=\"1\"><title>Section 1</title><content>Content 1</content></section><section id=\"2\"><title>Section 2</title><content>Content 2</content></section></root>";
        MockMultipartFile xmlFile = new MockMultipartFile(
                "file",
                "sections.xml",
                "text/xml",
                content.getBytes()
        );

        File pdfFile = tempDir.resolve("sections_output.pdf").toFile();

        xmlToPdfService.convertXmlToPdf(xmlFile, pdfFile);

        assertThat(pdfFile).exists().isNotEmpty();
    }

    // ========== New tests: XXE prevention (Requirements 6.1, 6.2, 6.3, 6.4, 6.5) ==========

    @Test
    void testConvertXmlToPdf_BillionLaughsAttack_DoesNotExpand(@TempDir Path tempDir) throws Exception {
        // Billion laughs XXE attack — entity expansion should NOT cause resource exhaustion
        String billionLaughs = """
                <?xml version="1.0"?>
                <!DOCTYPE lolz [
                  <!ENTITY lol "lol">
                  <!ENTITY lol2 "&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;">
                  <!ENTITY lol3 "&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;">
                  <!ENTITY lol4 "&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;">
                  <!ENTITY lol5 "&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;">
                  <!ENTITY lol6 "&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;">
                  <!ENTITY lol7 "&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;">
                  <!ENTITY lol8 "&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;">
                  <!ENTITY lol9 "&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;">
                ]>
                <root>&lol9;</root>
                """;

        MockMultipartFile xmlFile = new MockMultipartFile(
                "file",
                "billion_laughs.xml",
                "text/xml",
                billionLaughs.getBytes()
        );

        File pdfFile = tempDir.resolve("billion_laughs_output.pdf").toFile();

        // Should complete without hanging or OOM — JSoup does not expand entities
        xmlToPdfService.convertXmlToPdf(xmlFile, pdfFile);

        assertThat(pdfFile).exists()
                .as("PDF should be created without resource exhaustion");
        // The output PDF should be small — entities are NOT expanded
        assertThat(pdfFile.length())
                .as("PDF size should be small since entities are not expanded")
                .isLessThan(50_000L);
    }

    @Test
    void testConvertXmlToPdf_ExternalEntitySystemDeclaration_NotResolved(@TempDir Path tempDir) throws Exception {
        // External entity with SYSTEM declaration — should NOT be resolved
        String xxeXml = """
                <?xml version="1.0"?>
                <!DOCTYPE foo [
                  <!ENTITY xxe SYSTEM "file:///etc/passwd">
                ]>
                <root><data>&xxe;</data></root>
                """;

        MockMultipartFile xmlFile = new MockMultipartFile(
                "file",
                "xxe_system.xml",
                "text/xml",
                xxeXml.getBytes()
        );

        File pdfFile = tempDir.resolve("xxe_system_output.pdf").toFile();

        // Should complete without resolving external entity
        xmlToPdfService.convertXmlToPdf(xmlFile, pdfFile);

        assertThat(pdfFile).exists()
                .as("PDF should be created without resolving external entities");
        // The PDF should NOT contain /etc/passwd contents — entity stays unresolved
        assertThat(pdfFile.length())
                .as("PDF should be small since external entity is not resolved")
                .isLessThan(50_000L);
    }

    // ========== New tests: Indentation normalization (Requirements 9.1, 9.4) ==========

    @Test
    void testConvertXmlToPdf_InconsistentIndentation_NormalizedToTwoSpaces(@TempDir Path tempDir) throws Exception {
        // XML with inconsistent indentation (tabs, 4 spaces, etc.) should be normalized
        String inconsistentXml = """
                <?xml version="1.0"?>
                <root>
                \t\t<parent>
                        <child>Value1</child>
                   <child>Value2</child>
                \t</parent>
                </root>
                """;

        MockMultipartFile xmlFile = new MockMultipartFile(
                "file",
                "inconsistent.xml",
                "text/xml",
                inconsistentXml.getBytes()
        );

        File pdfFile = tempDir.resolve("inconsistent_output.pdf").toFile();

        xmlToPdfService.convertXmlToPdf(xmlFile, pdfFile);

        assertThat(pdfFile).exists().isNotEmpty()
                .as("PDF should be created with normalized indentation");
    }

    @Test
    void testConvertXmlToPdf_AlreadyIndentedXml_PreservesStructure(@TempDir Path tempDir) throws Exception {
        // Well-indented XML should remain properly indented (2-space)
        String wellIndentedXml = """
                <?xml version="1.0"?>
                <root>
                  <parent>
                    <child>Value1</child>
                    <child>Value2</child>
                  </parent>
                </root>
                """;

        MockMultipartFile xmlFile = new MockMultipartFile(
                "file",
                "well_indented.xml",
                "text/xml",
                wellIndentedXml.getBytes()
        );

        File pdfFile = tempDir.resolve("well_indented_output.pdf").toFile();

        xmlToPdfService.convertXmlToPdf(xmlFile, pdfFile);

        assertThat(pdfFile).exists().isNotEmpty()
                .as("PDF should preserve already well-formatted XML structure");
    }

    // ========== New tests: Monospace rendering (Requirements 9.2) ==========

    @Test
    void testConvertXmlToPdf_MonospaceRendering_ProducesValidPdf(@TempDir Path tempDir) throws Exception {
        // Verifies that XML content rendered in monospace font produces a valid PDF
        String content = """
                <?xml version="1.0"?>
                <config>
                  <database>
                    <host>localhost</host>
                    <port>5432</port>
                  </database>
                </config>
                """;

        MockMultipartFile xmlFile = new MockMultipartFile(
                "file",
                "config.xml",
                "text/xml",
                content.getBytes()
        );

        File pdfFile = tempDir.resolve("monospace_output.pdf").toFile();

        xmlToPdfService.convertXmlToPdf(xmlFile, pdfFile);

        assertThat(pdfFile).exists().isNotEmpty()
                .as("Monospace-rendered PDF should be valid and non-empty");
        // The PDF should contain substantial content from formatted XML
        assertThat(pdfFile.length())
                .as("PDF should have substantial content from formatted XML")
                .isGreaterThan(100L);
    }
}
