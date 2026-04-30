package com.xtopdf.xtopdf.services.conversion.spreadsheet;

/**
 * Immutable formatting metadata for a single table cell.
 * Used to pass cell styling from the Excel extraction layer
 * to the PDF rendering layer.
 */
public record CellFormatting(
    boolean bold,
    int backgroundR,
    int backgroundG,
    int backgroundB,
    boolean hasBackground,
    String formattedValue
) {
    /** Plain cell with no formatting. */
    public static final CellFormatting PLAIN = new CellFormatting(false, 255, 255, 255, false, null);
}
