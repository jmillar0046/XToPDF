package com.xtopdf.xtopdf.services.conversion.document;

import lombok.extern.slf4j.Slf4j;

/**
 * Immutable RGB color value parsed from a DOCX run's hex color string.
 */
@Slf4j
record Color(int r, int g, int b) {

    static final Color BLACK = new Color(0, 0, 0);

    /**
     * Parses a 6-character hexadecimal RGB string (e.g., "FF0000") into a Color.
     * Returns {@link #BLACK} for null, wrong-length, or non-hex inputs.
     */
    static Color fromHex(String hexColor) {
        if (hexColor == null || hexColor.length() != 6) {
            return BLACK;
        }
        try {
            int r = Integer.parseInt(hexColor.substring(0, 2), 16);
            int g = Integer.parseInt(hexColor.substring(2, 4), 16);
            int b = Integer.parseInt(hexColor.substring(4, 6), 16);
            return new Color(r, g, b);
        } catch (NumberFormatException e) {
            log.warn("Invalid color hex string '{}', defaulting to black", hexColor);
            return BLACK;
        }
    }
}
