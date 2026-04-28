package com.xtopdf.xtopdf.services.conversion.document;

import net.jqwik.api.*;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Property-based test for invalid hex color defaults to black.
 *
 * **Validates: Requirements 3.5**
 *
 * Property 4: Invalid hex color defaults to black
 * For any string that is null, not exactly 6 characters long, or contains
 * non-hexadecimal characters, parseColor() SHALL return (0, 0, 0).
 */
class InvalidColorPropertyTest {

    private static final int[] BLACK = {0, 0, 0};

    private final DocxToPdfService service = new DocxToPdfService(
            new com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend());

    @Property(tries = 200)
    void nullColorDefaultsToBlack(@ForAll("nullProvider") String color) {
        int[] result = service.parseColor(color);
        assertArrayEquals(BLACK, result,
                "null input should return black (0,0,0)");
    }

    @Property(tries = 200)
    void wrongLengthStringDefaultsToBlack(@ForAll("wrongLengthStrings") String color) {
        int[] result = service.parseColor(color);
        assertArrayEquals(BLACK, result,
                String.format("Wrong-length string '%s' (length=%d) should return black",
                        color, color.length()));
    }

    @Property(tries = 200)
    void nonHexSixCharStringDefaultsToBlack(@ForAll("nonHexSixCharStrings") String color) {
        int[] result = service.parseColor(color);
        assertArrayEquals(BLACK, result,
                String.format("Non-hex string '%s' should return black", color));
    }

    @Provide
    Arbitrary<String> nullProvider() {
        return Arbitraries.just(null);
    }

    @Provide
    Arbitrary<String> wrongLengthStrings() {
        // Generate strings of any length except 6
        return Arbitraries.strings()
                .ofMinLength(0)
                .ofMaxLength(20)
                .filter(s -> s.length() != 6);
    }

    @Provide
    Arbitrary<String> nonHexSixCharStrings() {
        // Generate 6-char strings that contain at least one non-hex character
        return Arbitraries.strings()
                .withCharRange('G', 'Z')  // non-hex uppercase letters
                .ofLength(6);
    }
}
