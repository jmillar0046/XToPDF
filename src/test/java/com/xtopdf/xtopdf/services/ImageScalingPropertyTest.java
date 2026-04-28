package com.xtopdf.xtopdf.services;

import net.jqwik.api.*;
import net.jqwik.api.constraints.DoubleRange;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for image scaling logic.
 *
 * Property 5: Image scaling preserves aspect ratio
 *
 * For any image dimensions (W, H) and page constraints (maxWidth, maxHeight),
 * the scaled dimensions preserve the aspect ratio and fit within bounds.
 *
 * This tests the pure scaling math directly — no PDF generation needed.
 *
 * **Validates: Requirements 3.2**
 */
class ImageScalingPropertyTest {

    /**
     * Computes scaled dimensions for an image to fit within page constraints
     * while preserving aspect ratio. This mirrors the logic in PdfBoxDocumentBuilder.addImage().
     *
     * @param imageWidth   original image width (pixels)
     * @param imageHeight  original image height (pixels)
     * @param maxWidth     maximum allowed width (points)
     * @param maxHeight    maximum allowed height (points)
     * @return float array [scaledWidth, scaledHeight]
     */
    static float[] scaleImage(float imageWidth, float imageHeight, float maxWidth, float maxHeight) {
        float scale = Math.min(maxWidth / imageWidth, maxHeight / imageHeight);
        if (scale > 1) {
            scale = 1; // Don't upscale
        }
        return new float[] { imageWidth * scale, imageHeight * scale };
    }

    /**
     * Property 5: Image scaling preserves aspect ratio
     *
     * For any image dimensions (W, H) and page constraints (maxWidth, maxHeight),
     * the resulting scaled dimensions satisfy:
     * - scaledWidth / scaledHeight == W / H (within floating-point tolerance)
     * - scaledWidth <= maxWidth
     * - scaledHeight <= maxHeight
     *
     * **Validates: Requirements 3.2**
     */
    @Property(tries = 1000)
    @Label("Image scaling preserves aspect ratio and fits within bounds")
    void imageScalingPreservesAspectRatio(
            @ForAll("imageDimensions") float imageWidth,
            @ForAll("imageDimensions") float imageHeight,
            @ForAll("pageDimensions") float maxWidth,
            @ForAll("pageDimensions") float maxHeight) {

        float[] scaled = scaleImage(imageWidth, imageHeight, maxWidth, maxHeight);
        float scaledWidth = scaled[0];
        float scaledHeight = scaled[1];

        // Scaled dimensions must fit within bounds
        assertThat(scaledWidth)
                .as("Scaled width (%f) should be <= maxWidth (%f)", scaledWidth, maxWidth)
                .isLessThanOrEqualTo(maxWidth + 0.01f);

        assertThat(scaledHeight)
                .as("Scaled height (%f) should be <= maxHeight (%f)", scaledHeight, maxHeight)
                .isLessThanOrEqualTo(maxHeight + 0.01f);

        // Aspect ratio must be preserved (within floating-point tolerance)
        float originalRatio = imageWidth / imageHeight;
        float scaledRatio = scaledWidth / scaledHeight;

        assertThat(scaledRatio)
                .as("Aspect ratio should be preserved: original=%f, scaled=%f", originalRatio, scaledRatio)
                .isCloseTo(originalRatio, org.assertj.core.data.Offset.offset(0.01f));

        // Scaled dimensions should not exceed original (no upscaling)
        assertThat(scaledWidth)
                .as("Scaled width should not exceed original width")
                .isLessThanOrEqualTo(imageWidth + 0.01f);

        assertThat(scaledHeight)
                .as("Scaled height should not exceed original height")
                .isLessThanOrEqualTo(imageHeight + 0.01f);
    }

    /**
     * Property: When image fits within bounds, no scaling occurs.
     */
    @Property(tries = 500)
    @Label("Small images are not upscaled")
    void smallImagesAreNotUpscaled(
            @ForAll("smallImageDimensions") float imageWidth,
            @ForAll("smallImageDimensions") float imageHeight) {

        float maxWidth = 500f;
        float maxHeight = 700f;

        float[] scaled = scaleImage(imageWidth, imageHeight, maxWidth, maxHeight);

        // Image should remain at original size (no upscaling)
        assertThat(scaled[0])
                .as("Small image width should not be upscaled")
                .isCloseTo(imageWidth, org.assertj.core.data.Offset.offset(0.01f));

        assertThat(scaled[1])
                .as("Small image height should not be upscaled")
                .isCloseTo(imageHeight, org.assertj.core.data.Offset.offset(0.01f));
    }

    // ---------------------------------------------------------------
    // Arbitraries / Generators
    // ---------------------------------------------------------------

    @Provide
    Arbitrary<Float> imageDimensions() {
        return Arbitraries.floats().between(1f, 5000f);
    }

    @Provide
    Arbitrary<Float> pageDimensions() {
        return Arbitraries.floats().between(100f, 1000f);
    }

    @Provide
    Arbitrary<Float> smallImageDimensions() {
        return Arbitraries.floats().between(1f, 100f);
    }
}
