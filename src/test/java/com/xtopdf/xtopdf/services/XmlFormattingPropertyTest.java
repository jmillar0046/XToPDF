package com.xtopdf.xtopdf.services;

import net.jqwik.api.*;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for XML formatting idempotence.
 *
 * // Feature: converter-improvements, Property 7: XML formatting idempotence
 *
 * Property 7: XML formatting idempotence
 * - For any valid XML input string, formatting it via JSoup's XML parser with
 *   indentAmount(2) and then formatting the result again SHALL produce byte-identical output.
 * - That is: format(format(xml)) == format(xml).
 *
 * **Validates: Requirements 9.1, 9.4**
 */
class XmlFormattingPropertyTest {

    // Feature: converter-improvements, Property 7: XML formatting idempotence

    /**
     * Formats XML using the same approach as XmlToPdfService:
     * parse with JSoup xmlParser(), set indentAmount(2), output html().
     */
    private String formatXml(String xml) {
        org.jsoup.nodes.Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        doc.outputSettings()
                .indentAmount(2)
                .outline(false)
                .charset(StandardCharsets.UTF_8);
        return doc.html();
    }

    /**
     * Property 7: XML formatting is idempotent.
     * Formatting any valid XML document once and then formatting the result again
     * produces identical output: format(format(xml)) == format(xml).
     *
     * **Validates: Requirements 9.1, 9.4**
     */
    @Property(tries = 25)
    @Tag("Feature: converter-improvements, Property 7: XML formatting idempotence")
    void xmlFormattingIsIdempotent(@ForAll("validXmlDocuments") String xml) {
        String firstFormat = formatXml(xml);
        String secondFormat = formatXml(firstFormat);

        assertThat(secondFormat)
                .as("format(format(xml)) must equal format(xml) - formatting must be idempotent")
                .isEqualTo(firstFormat);
    }

    @Provide
    Arbitrary<String> validXmlDocuments() {
        return Arbitraries.oneOf(
                flatXmlDocuments(),
                nestedXmlDocuments(),
                xmlWithAttributes(),
                xmlWithMixedContent(),
                xmlWithMultipleChildren()
        );
    }

    /**
     * Generates flat XML documents with a single root element and text content.
     */
    private Arbitrary<String> flatXmlDocuments() {
        Arbitrary<String> elementNames = Arbitraries.strings()
                .alpha().ofMinLength(1).ofMaxLength(8);
        Arbitrary<String> textContent = Arbitraries.strings()
                .alpha().ofMinLength(0).ofMaxLength(30);

        return Combinators.combine(elementNames, textContent)
                .as((elem, text) ->
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<" + elem + ">" + text + "</" + elem + ">"
                );
    }

    /**
     * Generates XML documents with varying nesting depth (2-5 levels).
     */
    private Arbitrary<String> nestedXmlDocuments() {
        Arbitrary<String> elementNames = Arbitraries.strings()
                .alpha().ofMinLength(1).ofMaxLength(8);
        Arbitrary<Integer> depths = Arbitraries.integers().between(2, 5);
        Arbitrary<String> textContent = Arbitraries.strings()
                .alpha().ofMinLength(1).ofMaxLength(20);

        return Combinators.combine(elementNames, depths, textContent)
                .as((elem, depth, text) -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                    for (int i = 0; i < depth; i++) {
                        String tag = elem + i;
                        sb.append("<").append(tag).append(">");
                    }
                    sb.append(text);
                    for (int i = depth - 1; i >= 0; i--) {
                        String tag = elem + i;
                        sb.append("</").append(tag).append(">");
                    }
                    return sb.toString();
                });
    }

    /**
     * Generates XML documents with attributes on elements.
     */
    private Arbitrary<String> xmlWithAttributes() {
        Arbitrary<String> elementNames = Arbitraries.strings()
                .alpha().ofMinLength(1).ofMaxLength(8);
        Arbitrary<String> attrNames = Arbitraries.strings()
                .alpha().ofMinLength(1).ofMaxLength(6);
        Arbitrary<String> attrValues = Arbitraries.strings()
                .alpha().ofMinLength(1).ofMaxLength(10);
        Arbitrary<String> textContent = Arbitraries.strings()
                .alpha().ofMinLength(0).ofMaxLength(20);
        Arbitrary<Integer> attrCount = Arbitraries.integers().between(1, 3);

        return Combinators.combine(elementNames, attrNames, attrValues, textContent, attrCount)
                .as((elem, attrName, attrVal, text, count) -> {
                    StringBuilder attrs = new StringBuilder();
                    for (int i = 0; i < count; i++) {
                        attrs.append(" ").append(attrName).append(i)
                                .append("=\"").append(attrVal).append("\"");
                    }
                    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<" + elem + attrs + ">" + text + "</" + elem + ">";
                });
    }

    /**
     * Generates XML documents with mixed text and child elements.
     */
    private Arbitrary<String> xmlWithMixedContent() {
        Arbitrary<String> rootNames = Arbitraries.strings()
                .alpha().ofMinLength(1).ofMaxLength(8);
        Arbitrary<String> childNames = Arbitraries.strings()
                .alpha().ofMinLength(1).ofMaxLength(6);
        Arbitrary<String> textContent = Arbitraries.strings()
                .alpha().ofMinLength(1).ofMaxLength(15);

        return Combinators.combine(rootNames, childNames, textContent)
                .as((root, child, text) ->
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<" + root + ">" + text +
                        "<" + child + ">" + text + "</" + child + ">" +
                        text + "</" + root + ">"
                );
    }

    /**
     * Generates XML documents with multiple sibling child elements.
     */
    private Arbitrary<String> xmlWithMultipleChildren() {
        Arbitrary<String> rootNames = Arbitraries.strings()
                .alpha().ofMinLength(1).ofMaxLength(8);
        Arbitrary<String> childNames = Arbitraries.strings()
                .alpha().ofMinLength(1).ofMaxLength(6);
        Arbitrary<String> textContent = Arbitraries.strings()
                .alpha().ofMinLength(1).ofMaxLength(10);
        Arbitrary<Integer> childCount = Arbitraries.integers().between(2, 5);

        return Combinators.combine(rootNames, childNames, textContent, childCount)
                .as((root, child, text, count) -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                    sb.append("<").append(root).append(">");
                    for (int i = 0; i < count; i++) {
                        String tag = child + i;
                        sb.append("<").append(tag).append(">").append(text).append("</").append(tag).append(">");
                    }
                    sb.append("</").append(root).append(">");
                    return sb.toString();
                });
    }
}
