package com.xtopdf.xtopdf.services.conversion.image;

import net.jqwik.api.*;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for SVG rendering using Apache Batik.
 * Validates: Requirements 9.2, 9.3, 9.4, 9.5
 *
 * Property 18: SVG Feature Rendering
 * Property 19: SVG Animation Conversion
 */
class SvgRenderingPropertyTest {

    private final SvgToPdfService svgToPdfService = new SvgToPdfService();

    /**
     * Property 18: SVG Feature Rendering
     *
     * Various SVG structures with different features (paths, shapes, gradients, text)
     * should produce valid PDF output.
     *
     * **Validates: Requirements 9.2, 9.3, 9.4**
     */
    @Property(tries = 25)
    @Tag("Feature: svg-rendering, Property 18: SVG Feature Rendering")
    void svgWithVariousFeaturesProducesValidPdf(
            @ForAll("svgDocuments") String svgContent) throws IOException {

        var tempFile = File.createTempFile("svg-test-", ".pdf");
        try {
            var multipartFile = new MockMultipartFile(
                    "file", "test.svg", "image/svg+xml", svgContent.getBytes());

            svgToPdfService.convertSvgToPdf(multipartFile, tempFile);

            assertThat(tempFile).exists();
            assertThat(tempFile.length()).isGreaterThan(0);

            // Verify PDF magic bytes
            byte[] pdfBytes = Files.readAllBytes(tempFile.toPath());
            assertThat(pdfBytes.length).isGreaterThan(4);
            assertThat(new String(pdfBytes, 0, 5)).startsWith("%PDF");
        } finally {
            tempFile.delete();
        }
    }

    /**
     * Property 19: SVG Animation Conversion
     *
     * SVGs containing animation elements should have animations stripped
     * and still produce valid static PDF output.
     *
     * **Validates: Requirements 9.5**
     */
    @Property(tries = 25)
    @Tag("Feature: svg-rendering, Property 19: SVG Animation Conversion")
    void svgWithAnimationsProducesStaticPdf(
            @ForAll("svgWithAnimations") String svgContent) throws IOException {

        var tempFile = File.createTempFile("svg-anim-", ".pdf");
        try {
            var multipartFile = new MockMultipartFile(
                    "file", "animated.svg", "image/svg+xml", svgContent.getBytes());

            svgToPdfService.convertSvgToPdf(multipartFile, tempFile);

            assertThat(tempFile).exists();
            assertThat(tempFile.length()).isGreaterThan(0);

            byte[] pdfBytes = Files.readAllBytes(tempFile.toPath());
            assertThat(new String(pdfBytes, 0, 5)).startsWith("%PDF");
        } finally {
            tempFile.delete();
        }
    }

    @Provide
    Arbitrary<String> svgDocuments() {
        return Arbitraries.of(
                // Simple rectangle
                svgWrap("<rect x=\"10\" y=\"10\" width=\"100\" height=\"80\" fill=\"blue\" />"),
                // Circle
                svgWrap("<circle cx=\"100\" cy=\"100\" r=\"50\" fill=\"red\" />"),
                // Ellipse
                svgWrap("<ellipse cx=\"100\" cy=\"80\" rx=\"80\" ry=\"50\" fill=\"green\" />"),
                // Line
                svgWrap("<line x1=\"0\" y1=\"0\" x2=\"200\" y2=\"200\" stroke=\"black\" stroke-width=\"2\" />"),
                // Path
                svgWrap("<path d=\"M10 80 C 40 10, 65 10, 95 80 S 150 150, 180 80\" stroke=\"black\" fill=\"transparent\" />"),
                // Polygon
                svgWrap("<polygon points=\"100,10 40,198 190,78 10,78 160,198\" fill=\"lime\" stroke=\"purple\" stroke-width=\"2\" />"),
                // Text
                svgWrap("<text x=\"50\" y=\"100\" font-size=\"20\" fill=\"black\">Hello SVG</text>"),
                // Group with transform
                svgWrap("<g transform=\"translate(50,50)\"><rect width=\"50\" height=\"50\" fill=\"orange\" /><circle cx=\"25\" cy=\"25\" r=\"10\" fill=\"white\" /></g>"),
                // Linear gradient
                svgWrap("<defs><linearGradient id=\"grad1\" x1=\"0%\" y1=\"0%\" x2=\"100%\" y2=\"0%\"><stop offset=\"0%\" style=\"stop-color:rgb(255,255,0)\" /><stop offset=\"100%\" style=\"stop-color:rgb(255,0,0)\" /></linearGradient></defs><rect width=\"200\" height=\"100\" fill=\"url(#grad1)\" />"),
                // Multiple shapes
                svgWrap("<rect x=\"10\" y=\"10\" width=\"80\" height=\"80\" fill=\"navy\" /><circle cx=\"150\" cy=\"50\" r=\"40\" fill=\"crimson\" /><text x=\"50\" y=\"150\" fill=\"black\">Shapes</text>")
        );
    }

    @Provide
    Arbitrary<String> svgWithAnimations() {
        return Arbitraries.of(
                // Rectangle with animate
                svgWrap("<rect x=\"10\" y=\"10\" width=\"100\" height=\"80\" fill=\"blue\"><animate attributeName=\"x\" from=\"10\" to=\"150\" dur=\"2s\" repeatCount=\"indefinite\" /></rect>"),
                // Circle with animateTransform
                svgWrap("<circle cx=\"100\" cy=\"100\" r=\"50\" fill=\"red\"><animateTransform attributeName=\"transform\" type=\"rotate\" from=\"0 100 100\" to=\"360 100 100\" dur=\"3s\" repeatCount=\"indefinite\" /></circle>"),
                // Path with animateMotion
                svgWrap("<circle r=\"10\" fill=\"green\"><animateMotion dur=\"5s\" repeatCount=\"indefinite\"><mpath href=\"#path1\" /></animateMotion></circle><path id=\"path1\" d=\"M10,80 C40,10 65,10 95,80\" fill=\"none\" stroke=\"gray\" />"),
                // Element with set
                svgWrap("<rect x=\"10\" y=\"10\" width=\"100\" height=\"80\" fill=\"blue\"><set attributeName=\"fill\" to=\"red\" begin=\"2s\" /></rect>"),
                // Self-closing animate
                svgWrap("<rect x=\"10\" y=\"10\" width=\"100\" height=\"80\" fill=\"purple\"><animate attributeName=\"width\" from=\"100\" to=\"200\" dur=\"1s\" /></rect>")
        );
    }

    private String svgWrap(String content) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
               "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"300\" height=\"300\" viewBox=\"0 0 300 300\">" +
               content +
               "</svg>";
    }
}
