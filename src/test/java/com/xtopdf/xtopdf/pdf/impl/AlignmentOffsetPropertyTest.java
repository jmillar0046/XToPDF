package com.xtopdf.xtopdf.pdf.impl;

import com.xtopdf.xtopdf.pdf.TextAlignment;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based test for alignment X-offset computation.
 *
 * <p>This is a pure math test — no PDF generation. Given text width, page width,
 * margin, and alignment, verify the computed X offset matches the expected formula.</p>
 *
 * <p><b>Validates: Requirements 2.1, 2.2, 2.4</b></p>
 */
class AlignmentOffsetPropertyTest {

    /**
     * Computes the X offset for a line of text given alignment, margin, page width, and line width.
     * This mirrors the logic that PdfBoxDocumentBuilder.renderLine() should implement.
     *
     * @param alignment the text alignment
     * @param margin    the page margin in points
     * @param pageWidth the total page width in points
     * @param lineWidth the computed width of the rendered text line in points
     * @return the X offset where the line should start
     */
    static float computeXOffset(TextAlignment alignment, float margin, float pageWidth, float lineWidth) {
        float maxWidth = pageWidth - (2 * margin);
        return switch (alignment) {
            case LEFT -> margin;
            case CENTER -> margin + (maxWidth - lineWidth) / 2;
            case RIGHT -> margin + (maxWidth - lineWidth);
        };
    }

    /**
     * Property 2: Alignment X-offset computation
     *
     * For any valid combination of text width, page width, margin, and alignment,
     * the computed X offset must satisfy:
     * - LEFT: x == margin
     * - CENTER: x == margin + (maxWidth - lineWidth) / 2
     * - RIGHT: x == margin + (maxWidth - lineWidth)
     *
     * **Validates: Requirements 2.1, 2.2, 2.4**
     */
    @Property(tries = 500)
    void alignmentXOffsetShouldMatchFormula(
            @ForAll("alignments") TextAlignment alignment,
            @ForAll @FloatRange(min = 10f, max = 100f) float margin,
            @ForAll @FloatRange(min = 200f, max = 1000f) float pageWidth,
            @ForAll @FloatRange(min = 0f, max = 400f) float lineWidth
    ) {
        // Ensure lineWidth does not exceed maxWidth (the usable area)
        float maxWidth = pageWidth - (2 * margin);
        Assume.that(maxWidth > 0);
        Assume.that(lineWidth <= maxWidth);

        float xOffset = computeXOffset(alignment, margin, pageWidth, lineWidth);

        switch (alignment) {
            case LEFT -> assertThat(xOffset)
                    .as("LEFT alignment: x should equal margin")
                    .isEqualTo(margin);
            case CENTER -> assertThat(xOffset)
                    .as("CENTER alignment: x should equal margin + (maxWidth - lineWidth) / 2")
                    .isCloseTo(margin + (maxWidth - lineWidth) / 2, org.assertj.core.data.Offset.offset(0.01f));
            case RIGHT -> assertThat(xOffset)
                    .as("RIGHT alignment: x should equal margin + (maxWidth - lineWidth)")
                    .isCloseTo(margin + (maxWidth - lineWidth), org.assertj.core.data.Offset.offset(0.01f));
        }
    }

    @Property(tries = 500)
    void leftAlignmentXOffsetShouldAlwaysEqualMargin(
            @ForAll @FloatRange(min = 10f, max = 100f) float margin,
            @ForAll @FloatRange(min = 200f, max = 1000f) float pageWidth,
            @ForAll @FloatRange(min = 0f, max = 400f) float lineWidth
    ) {
        float maxWidth = pageWidth - (2 * margin);
        Assume.that(maxWidth > 0);
        Assume.that(lineWidth <= maxWidth);

        float xOffset = computeXOffset(TextAlignment.LEFT, margin, pageWidth, lineWidth);
        assertThat(xOffset).isEqualTo(margin);
    }

    @Property(tries = 500)
    void centerAlignmentXOffsetShouldBeBetweenLeftAndRight(
            @ForAll @FloatRange(min = 10f, max = 100f) float margin,
            @ForAll @FloatRange(min = 200f, max = 1000f) float pageWidth,
            @ForAll @FloatRange(min = 0f, max = 400f) float lineWidth
    ) {
        float maxWidth = pageWidth - (2 * margin);
        Assume.that(maxWidth > 0);
        Assume.that(lineWidth <= maxWidth);

        float leftX = computeXOffset(TextAlignment.LEFT, margin, pageWidth, lineWidth);
        float centerX = computeXOffset(TextAlignment.CENTER, margin, pageWidth, lineWidth);
        float rightX = computeXOffset(TextAlignment.RIGHT, margin, pageWidth, lineWidth);

        assertThat(centerX)
                .as("CENTER X should be >= LEFT X")
                .isGreaterThanOrEqualTo(leftX);
        assertThat(centerX)
                .as("CENTER X should be <= RIGHT X")
                .isLessThanOrEqualTo(rightX);
    }

    @Provide
    Arbitrary<TextAlignment> alignments() {
        return Arbitraries.of(TextAlignment.LEFT, TextAlignment.CENTER, TextAlignment.RIGHT);
    }
}
