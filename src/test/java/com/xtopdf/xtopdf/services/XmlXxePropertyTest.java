package com.xtopdf.xtopdf.services;

import net.jqwik.api.*;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for XML XXE prevention.
 *
 * // Feature: converter-improvements, Property 4: XML parsing prevents XXE
 *
 * Property 4: XML parsing prevents XXE (no entity resolution)
 * - For any XML string containing DOCTYPE declarations with SYSTEM or PUBLIC
 *   external entity references, parsing via JSoup's Parser.xmlParser() SHALL NOT
 *   resolve external resources.
 * - The parsed output SHALL contain the literal entity reference text (unresolved)
 *   or omit it entirely.
 *
 * **Validates: Requirements 6.1, 6.2, 6.3**
 */
class XmlXxePropertyTest {

    /**
     * Property 4: External SYSTEM entity references are not resolved by JSoup XML parser.
     * Generates XML with random DOCTYPE SYSTEM entity declarations pointing to file:// or http:// URIs.
     * Verifies the parsed output does not contain content that would result from resolving those URIs.
     *
     * **Validates: Requirements 6.1, 6.2, 6.3**
     */
    @Property(tries = 25)
    @Tag("Feature: converter-improvements, Property 4: XML parsing prevents XXE")
    void systemEntityReferencesAreNotResolved(
            @ForAll("xmlWithSystemEntity") String maliciousXml) {

        // Parse using JSoup's XML parser (the same approach the XmlToPdfService uses)
        org.jsoup.nodes.Document doc = Jsoup.parse(maliciousXml, "", Parser.xmlParser());

        // Strip DOCTYPE declarations (same as XmlToPdfService does)
        doc.childNodes().stream()
                .filter(n -> n instanceof org.jsoup.nodes.DocumentType)
                .toList()
                .forEach(org.jsoup.nodes.Node::remove);

        doc.outputSettings()
                .indentAmount(2)
                .outline(false)
                .charset(StandardCharsets.UTF_8);

        String output = doc.html();

        // The output must NOT contain resolved file content markers
        // If entities were resolved, we'd see the sentinel values from the SYSTEM URIs
        assertThat(output)
                .as("JSoup XML parser must not resolve SYSTEM entity to file content")
                .doesNotContain("root:x:0:0:")       // /etc/passwd content
                .doesNotContain("RESOLVED_ENTITY")   // our sentinel marker
                .doesNotContain("[RESOLVED]");        // another sentinel marker
    }

    /**
     * Property 4: External PUBLIC entity references are not resolved by JSoup XML parser.
     * Generates XML with random DOCTYPE PUBLIC entity declarations.
     * Verifies the parsed output does not contain content from external resolution.
     *
     * **Validates: Requirements 6.1, 6.2, 6.3**
     */
    @Property(tries = 25)
    @Tag("Feature: converter-improvements, Property 4: XML parsing prevents XXE")
    void publicEntityReferencesAreNotResolved(
            @ForAll("xmlWithPublicEntity") String maliciousXml) {

        org.jsoup.nodes.Document doc = Jsoup.parse(maliciousXml, "", Parser.xmlParser());

        // Strip DOCTYPE declarations (same as XmlToPdfService does)
        doc.childNodes().stream()
                .filter(n -> n instanceof org.jsoup.nodes.DocumentType)
                .toList()
                .forEach(org.jsoup.nodes.Node::remove);

        doc.outputSettings()
                .indentAmount(2)
                .outline(false)
                .charset(StandardCharsets.UTF_8);

        String output = doc.html();

        assertThat(output)
                .as("JSoup XML parser must not resolve PUBLIC entity references")
                .doesNotContain("RESOLVED_ENTITY")
                .doesNotContain("[RESOLVED]")
                .doesNotContain("root:x:0:0:");
    }

    /**
     * Property 4: Billion laughs entity expansion does not cause resource exhaustion.
     * Generates XML with nested entity definitions (billion laughs pattern).
     * Verifies parsing completes without resolving/expanding the entities exponentially.
     *
     * **Validates: Requirements 6.1, 6.2, 6.3**
     */
    @Property(tries = 25)
    @Tag("Feature: converter-improvements, Property 4: XML parsing prevents XXE")
    void billionLaughsEntityExpansionDoesNotExplode(
            @ForAll("xmlWithBillionLaughs") String maliciousXml) {

        org.jsoup.nodes.Document doc = Jsoup.parse(maliciousXml, "", Parser.xmlParser());
        doc.outputSettings()
                .indentAmount(2)
                .outline(false)
                .charset(StandardCharsets.UTF_8);

        String output = doc.html();

        // If billion laughs were expanded, the output would be enormously large
        // JSoup does not expand entities, so output should be reasonably sized
        assertThat(output.length())
                .as("Output should not explode from entity expansion (billion laughs)")
                .isLessThan(maliciousXml.length() * 10);
    }

    @Provide
    Arbitrary<String> xmlWithSystemEntity() {
        Arbitrary<String> elementNames = Arbitraries.strings()
                .withCharRange('a', 'z').ofMinLength(1).ofMaxLength(8);

        Arbitrary<String> entityNames = Arbitraries.strings()
                .withCharRange('a', 'z').ofMinLength(2).ofMaxLength(6);

        Arbitrary<String> systemUris = Arbitraries.of(
                "file:///etc/passwd",
                "file:///etc/shadow",
                "file:///etc/hostname",
                "http://evil.example.com/RESOLVED_ENTITY",
                "https://evil.example.com/[RESOLVED]",
                "file:///proc/self/environ",
                "file:///dev/random",
                "http://169.254.169.254/latest/meta-data/"
        );

        Arbitrary<String> textContent = Arbitraries.strings()
                .withCharRange('a', 'z').ofMinLength(1).ofMaxLength(20);

        return Combinators.combine(elementNames, entityNames, systemUris, textContent)
                .as((elem, entity, uri, text) ->
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<!DOCTYPE " + elem + " [\n" +
                        "  <!ENTITY " + entity + " SYSTEM \"" + uri + "\">\n" +
                        "]>\n" +
                        "<" + elem + ">&" + entity + ";" + text + "</" + elem + ">"
                );
    }

    @Provide
    Arbitrary<String> xmlWithPublicEntity() {
        Arbitrary<String> elementNames = Arbitraries.strings()
                .withCharRange('a', 'z').ofMinLength(1).ofMaxLength(8);

        Arbitrary<String> entityNames = Arbitraries.strings()
                .withCharRange('a', 'z').ofMinLength(2).ofMaxLength(6);

        Arbitrary<String> publicIds = Arbitraries.of(
                "-//OASIS//DTD DocBook XML V4.5//EN",
                "-//W3C//DTD XHTML 1.0 Strict//EN",
                "-//Evil Corp//DTD Malicious//EN"
        );

        Arbitrary<String> systemUris = Arbitraries.of(
                "http://evil.example.com/RESOLVED_ENTITY",
                "https://evil.example.com/[RESOLVED]",
                "http://169.254.169.254/latest/meta-data/",
                "file:///etc/passwd"
        );

        Arbitrary<String> textContent = Arbitraries.strings()
                .withCharRange('a', 'z').ofMinLength(1).ofMaxLength(20);

        return Combinators.combine(elementNames, entityNames, publicIds, systemUris, textContent)
                .as((elem, entity, pubId, sysUri, text) ->
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<!DOCTYPE " + elem + " [\n" +
                        "  <!ENTITY " + entity + " PUBLIC \"" + pubId + "\" \"" + sysUri + "\">\n" +
                        "]>\n" +
                        "<" + elem + ">&" + entity + ";" + text + "</" + elem + ">"
                );
    }

    @Provide
    Arbitrary<String> xmlWithBillionLaughs() {
        Arbitrary<String> elementNames = Arbitraries.strings()
                .withCharRange('a', 'z').ofMinLength(1).ofMaxLength(8);

        // Generate varying depth of nested entities (3-6 levels)
        Arbitrary<Integer> depths = Arbitraries.integers().between(3, 6);

        return Combinators.combine(elementNames, depths)
                .as((elem, depth) -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                    sb.append("<!DOCTYPE ").append(elem).append(" [\n");
                    sb.append("  <!ENTITY lol \"lol\">\n");
                    for (int i = 1; i <= depth; i++) {
                        sb.append("  <!ENTITY lol").append(i).append(" \"");
                        String prev = (i == 1) ? "&lol;" : "&lol" + (i - 1) + ";";
                        // Each level references the previous 10 times
                        sb.append(prev.repeat(10));
                        sb.append("\">\n");
                    }
                    sb.append("]>\n");
                    sb.append("<").append(elem).append(">&lol").append(depth).append(";</").append(elem).append(">");
                    return sb.toString();
                });
    }
}
