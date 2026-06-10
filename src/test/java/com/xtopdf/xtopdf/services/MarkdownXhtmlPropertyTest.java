package com.xtopdf.xtopdf.services;

import net.jqwik.api.*;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * Property-based tests for Markdown XHTML sanitization.
 *
 * // Feature: converter-improvements, Property 1: XHTML sanitization produces valid XHTML
 * // Feature: converter-improvements, Property 2: External URL stripping prevents SSRF
 *
 * Property 1: XHTML sanitization produces valid XHTML
 * - For any HTML string produced by Commonmark's HtmlRenderer, the convertToXhtml() method
 *   SHALL produce output that is parseable as well-formed XML with the
 *   http://www.w3.org/1999/xhtml namespace on the root element.
 *
 * Property 2: External URL stripping prevents SSRF
 * - For any HTML string containing http:// or https:// URL references in link, img, script,
 *   iframe, object, or embed elements, the convertToXhtml() output SHALL NOT contain any of
 *   those elements with external URL references.
 *
 * **Validates: Requirements 1.2, 1.9**
 */
class MarkdownXhtmlPropertyTest {

    // Feature: converter-improvements, Property 1: XHTML sanitization produces valid XHTML
    // Feature: converter-improvements, Property 2: External URL stripping prevents SSRF

    private final Parser markdownParser = Parser.builder().build();
    private final HtmlRenderer htmlRenderer = HtmlRenderer.builder().build();

    /**
     * Converts HTML to XHTML using the same logic as MarkdownToPdfService/HtmlToPdfService:
     * JSoup parse → XML syntax → XHTML escape mode → namespace → strip external URLs.
     */
    private String convertToXhtml(String html) {
        Document doc = Jsoup.parse(html);
        doc.outputSettings()
                .syntax(Document.OutputSettings.Syntax.xml)
                .escapeMode(Entities.EscapeMode.xhtml)
                .charset(StandardCharsets.UTF_8);

        var htmlElement = doc.selectFirst("html");
        if (htmlElement != null && !htmlElement.hasAttr("xmlns")) {
            htmlElement.attr("xmlns", "http://www.w3.org/1999/xhtml");
        }

        // Strip external URLs and dangerous elements (SSRF prevention)
        doc.select("link[href~=(?i)^(https?:)?//]").remove();
        doc.select("img[src~=(?i)^(https?:)?//]").remove();
        doc.select("script, iframe, object, embed").remove();

        return doc.html();
    }

    /**
     * Property 1: XHTML sanitization produces valid XHTML.
     * For any Markdown input rendered to HTML by Commonmark, convertToXhtml() produces
     * well-formed XML with the XHTML namespace on the root element.
     *
     * **Validates: Requirements 1.2**
     */
    @Property(tries = 25)
    @Tag("Feature: converter-improvements, Property 1: XHTML sanitization produces valid XHTML")
    void xhtmlSanitizationProducesValidXhtml(@ForAll("randomMarkdown") String markdown) {
        // Render Markdown to HTML via Commonmark
        Node document = markdownParser.parse(markdown);
        String rawHtml = htmlRenderer.render(document);

        // Sanitize to XHTML
        String xhtml = convertToXhtml(rawHtml);

        // Verify output is well-formed XML (parseable without exceptions)
        assertThatNoException()
                .as("convertToXhtml() output must be parseable as well-formed XML")
                .isThrownBy(() -> {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    factory.setNamespaceAware(true);
                    factory.newDocumentBuilder()
                            .parse(new ByteArrayInputStream(xhtml.getBytes(StandardCharsets.UTF_8)));
                });

        // Verify root <html> element has the XHTML namespace
        Document parsedDoc = Jsoup.parse(xhtml);
        var htmlElement = parsedDoc.selectFirst("html");
        assertThat(htmlElement)
                .as("output must contain an <html> root element")
                .isNotNull();
        assertThat(htmlElement.attr("xmlns"))
                .as("root <html> element must have xmlns=\"http://www.w3.org/1999/xhtml\"")
                .isEqualTo("http://www.w3.org/1999/xhtml");
    }

    /**
     * Property 2: External URL stripping prevents SSRF.
     * For any HTML with http/https URLs in link/img/script/iframe/object/embed elements,
     * the output contains none of those external references.
     *
     * **Validates: Requirements 1.9**
     */
    @Property(tries = 25)
    @Tag("Feature: converter-improvements, Property 2: External URL stripping prevents SSRF")
    void externalUrlStrippingPreventsSsrf(@ForAll("htmlWithExternalUrls") String html) {
        String xhtml = convertToXhtml(html);

        // Parse the output to check for remaining external URL elements
        Document outputDoc = Jsoup.parse(xhtml);

        // Verify no link elements with external href remain
        assertThat(outputDoc.select("link[href~=(?i)^(https?:)?//]"))
                .as("output must not contain <link> elements with external URLs")
                .isEmpty();

        // Verify no img elements with external src remain
        assertThat(outputDoc.select("img[src~=(?i)^(https?:)?//]"))
                .as("output must not contain <img> elements with external URLs")
                .isEmpty();

        // Verify no script elements remain
        assertThat(outputDoc.select("script"))
                .as("output must not contain <script> elements")
                .isEmpty();

        // Verify no iframe elements remain
        assertThat(outputDoc.select("iframe"))
                .as("output must not contain <iframe> elements")
                .isEmpty();

        // Verify no object elements remain
        assertThat(outputDoc.select("object"))
                .as("output must not contain <object> elements")
                .isEmpty();

        // Verify no embed elements remain
        assertThat(outputDoc.select("embed"))
                .as("output must not contain <embed> elements")
                .isEmpty();
    }

    // --- Providers ---

    @Provide
    Arbitrary<String> randomMarkdown() {
        return Arbitraries.oneOf(
                markdownWithHeaders(),
                markdownWithBold(),
                markdownWithCode(),
                markdownWithLinks(),
                markdownWithNestedConstructs()
        );
    }

    @Provide
    Arbitrary<String> htmlWithExternalUrls() {
        return Arbitraries.oneOf(
                htmlWithExternalLinks(),
                htmlWithExternalImages(),
                htmlWithScriptElements(),
                htmlWithIframeElements(),
                htmlWithObjectEmbedElements(),
                htmlWithMixedExternalRefs()
        );
    }

    // --- Markdown generators ---

    /**
     * Generates Markdown with headers at various levels.
     */
    private Arbitrary<String> markdownWithHeaders() {
        Arbitrary<Integer> level = Arbitraries.integers().between(1, 6);
        Arbitrary<String> text = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20);

        return Combinators.combine(level, text)
                .as((lvl, txt) -> "#".repeat(lvl) + " " + txt + "\n\nSome body text.\n");
    }

    /**
     * Generates Markdown with bold and italic text.
     */
    private Arbitrary<String> markdownWithBold() {
        Arbitrary<String> boldText = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(15);
        Arbitrary<String> italicText = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(15);
        Arbitrary<String> normalText = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(15);

        return Combinators.combine(boldText, italicText, normalText)
                .as((bold, italic, normal) ->
                        "**" + bold + "** and *" + italic + "* and " + normal + "\n");
    }

    /**
     * Generates Markdown with inline and fenced code blocks.
     */
    private Arbitrary<String> markdownWithCode() {
        Arbitrary<String> inlineCode = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(15);
        Arbitrary<String> fencedCode = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(30);

        return Combinators.combine(inlineCode, fencedCode)
                .as((inline, fenced) ->
                        "Use `" + inline + "` inline.\n\n```\n" + fenced + "\n```\n");
    }

    /**
     * Generates Markdown with link constructs.
     */
    private Arbitrary<String> markdownWithLinks() {
        Arbitrary<String> linkText = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10);
        Arbitrary<String> domain = Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(10);

        return Combinators.combine(linkText, domain)
                .as((text, dom) ->
                        "Visit [" + text + "](https://" + dom + ".com) for details.\n");
    }

    /**
     * Generates Markdown with nested constructs (headers + bold + lists + code).
     */
    private Arbitrary<String> markdownWithNestedConstructs() {
        Arbitrary<String> text = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10);
        Arbitrary<Integer> headerLevel = Arbitraries.integers().between(1, 4);

        return Combinators.combine(text, headerLevel)
                .as((txt, lvl) ->
                        "#".repeat(lvl) + " " + txt + "\n\n" +
                        "- **" + txt + "** item\n" +
                        "- *" + txt + "* item\n" +
                        "- `" + txt + "` item\n\n" +
                        "```\ncode block: " + txt + "\n```\n");
    }

    // --- HTML with external URL generators ---

    /**
     * Generates HTML with external link elements.
     */
    private Arbitrary<String> htmlWithExternalLinks() {
        Arbitrary<String> domain = Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(10);

        return domain.map(dom ->
                "<html><head><link rel=\"stylesheet\" href=\"https://" + dom + ".com/style.css\" /></head>" +
                "<body><p>Content</p></body></html>");
    }

    /**
     * Generates HTML with external image elements.
     */
    private Arbitrary<String> htmlWithExternalImages() {
        Arbitrary<String> domain = Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(10);
        Arbitrary<String> path = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(8);

        return Combinators.combine(domain, path)
                .as((dom, p) ->
                        "<html><body><p>Text</p><img src=\"http://" + dom + ".com/" + p + ".png\" />" +
                        "<img src=\"https://" + dom + ".org/img/" + p + ".jpg\" /></body></html>");
    }

    /**
     * Generates HTML with script elements (should be removed entirely).
     */
    private Arbitrary<String> htmlWithScriptElements() {
        Arbitrary<String> domain = Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(10);

        return domain.map(dom ->
                "<html><body><script src=\"https://" + dom + ".com/evil.js\"></script>" +
                "<script>alert('xss')</script><p>Content</p></body></html>");
    }

    /**
     * Generates HTML with iframe elements (should be removed entirely).
     */
    private Arbitrary<String> htmlWithIframeElements() {
        Arbitrary<String> domain = Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(10);

        return domain.map(dom ->
                "<html><body><iframe src=\"https://" + dom + ".com/embed\"></iframe>" +
                "<p>Content</p></body></html>");
    }

    /**
     * Generates HTML with object and embed elements (should be removed entirely).
     */
    private Arbitrary<String> htmlWithObjectEmbedElements() {
        Arbitrary<String> domain = Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(10);

        return domain.map(dom ->
                "<html><body><object data=\"https://" + dom + ".com/flash.swf\"></object>" +
                "<embed src=\"http://" + dom + ".com/plugin\" /><p>Content</p></body></html>");
    }

    /**
     * Generates HTML with a mix of external references across multiple element types.
     */
    private Arbitrary<String> htmlWithMixedExternalRefs() {
        Arbitrary<String> domain = Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(8);

        return domain.map(dom ->
                "<html><head><link href=\"https://" + dom + ".com/a.css\" rel=\"stylesheet\" /></head>" +
                "<body><img src=\"http://" + dom + ".net/img.png\" />" +
                "<script src=\"https://" + dom + ".io/x.js\"></script>" +
                "<iframe src=\"https://" + dom + ".co/page\"></iframe>" +
                "<object data=\"http://" + dom + ".org/obj\"></object>" +
                "<embed src=\"https://" + dom + ".dev/embed\" />" +
                "<p>Safe content</p></body></html>");
    }
}
