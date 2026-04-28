package com.xtopdf.xtopdf.services.conversion.document;

import net.jqwik.api.*;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Property-based test for valid hex color round-trip parsing.
 *
 * **Validates: Requirements 3.1**
 *
 * Property 3: Valid hex color parsing round-trip
 * For any RGB triple (r, g, b) where each component is in [0, 255],
 * formatting it as a 6-character uppercase hex string and then parsing
 * it with parseColor() SHALL produce the original (r, g, b) values.
 */
class ColorParsingPropertyTest {

    private final DocxToPdfService service = new DocxToPdfService(
            new com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend());

    @Property(tries = 500)
    void validHexColorRoundTrip(
            @ForAll("rgbComponent") int r,
            @ForAll("rgbComponent") int g,
            @ForAll("rgbComponent") int b) {

        // Format as 6-char uppercase hex
        String hex = String.format("%02X%02X%02X", r, g, b);

        // Parse back using the actual service method
        int[] result = service.parseColor(hex);

        // Verify round-trip
        assertArrayEquals(new int[]{r, g, b}, result,
                String.format("Round-trip failed for hex '%s' (expected [%d,%d,%d] but got [%d,%d,%d])",
                        hex, r, g, b, result[0], result[1], result[2]));
    }

    @Provide
    Arbitrary<Integer> rgbComponent() {
        return Arbitraries.integers().between(0, 255);
    }
}
