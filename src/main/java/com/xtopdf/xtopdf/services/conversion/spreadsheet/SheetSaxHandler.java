package com.xtopdf.xtopdf.services.conversion.spreadsheet;

import org.apache.poi.xssf.model.SharedStrings;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * SAX handler for parsing XLSX sheet XML during streaming mode.
 * Extends DefaultHandler to process cell values row-by-row without
 * loading the entire sheet into memory.
 *
 * <p>Handles cell types: shared string (s), inline string (inlineStr),
 * number, and boolean. Resolves shared string references via the
 * provided SharedStrings table.</p>
 *
 * <p>Emits completed rows via a {@link RowCallback} functional interface
 * when the {@code </row>} end element is encountered.</p>
 */
public class SheetSaxHandler extends DefaultHandler {

    /**
     * Functional interface for receiving completed row data.
     */
    @FunctionalInterface
    public interface RowCallback {
        /**
         * Called when a complete row has been parsed.
         *
         * @param rowIndex the 0-based row index
         * @param cellValues the cell values for this row, indexed by column
         */
        void onRow(int rowIndex, List<String> cellValues);
    }

    private final SharedStrings sharedStrings;
    private final RowCallback rowCallback;

    // Current cell state
    private String currentCellRef;
    private String currentCellType;
    private StringBuilder cellValueBuilder;
    private boolean inValue;
    private boolean inInlineString;

    // Current row state
    private int currentRowIndex;
    private List<String> currentRowValues;
    private int maxColumnInRow;

    public SheetSaxHandler(SharedStrings sharedStrings, RowCallback rowCallback) {
        this.sharedStrings = sharedStrings;
        this.rowCallback = rowCallback;
        this.cellValueBuilder = new StringBuilder();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        switch (qName) {
            case "row" -> {
                String rowRef = attributes.getValue("r");
                currentRowIndex = rowRef != null ? Integer.parseInt(rowRef) - 1 : 0;
                currentRowValues = new ArrayList<>();
                maxColumnInRow = 0;
            }
            case "c" -> {
                currentCellRef = attributes.getValue("r");
                currentCellType = attributes.getValue("t");
                cellValueBuilder.setLength(0);
                inValue = false;
                inInlineString = false;
            }
            case "v" -> {
                inValue = true;
                cellValueBuilder.setLength(0);
            }
            case "is" -> {
                inInlineString = true;
                cellValueBuilder.setLength(0);
            }
            case "t" -> {
                if (inInlineString) {
                    // <t> inside <is> for inline strings
                    cellValueBuilder.setLength(0);
                }
            }
            default -> { /* ignore other elements */ }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        if (inValue || inInlineString) {
            cellValueBuilder.append(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        switch (qName) {
            case "v" -> {
                inValue = false;
            }
            case "is" -> {
                inInlineString = false;
            }
            case "c" -> {
                // Process the completed cell
                String value = resolveCellValue();
                int colIndex = cellRefToColumnIndex(currentCellRef);

                // Ensure the row values list is large enough
                while (currentRowValues.size() <= colIndex) {
                    currentRowValues.add("");
                }
                currentRowValues.set(colIndex, value);
                if (colIndex >= maxColumnInRow) {
                    maxColumnInRow = colIndex + 1;
                }
            }
            case "row" -> {
                // Emit the completed row
                if (currentRowValues != null) {
                    rowCallback.onRow(currentRowIndex, currentRowValues);
                }
            }
            default -> { /* ignore other elements */ }
        }
    }

    /**
     * Resolves the current cell's value based on its type.
     */
    private String resolveCellValue() {
        String rawValue = cellValueBuilder.toString();

        if (rawValue.isEmpty() && !inInlineString) {
            return "";
        }

        if ("s".equals(currentCellType)) {
            // Shared string reference
            try {
                int idx = Integer.parseInt(rawValue.trim());
                return sharedStrings.getItemAt(idx).getString();
            } catch (Exception e) {
                return rawValue;
            }
        } else if ("inlineStr".equals(currentCellType)) {
            // Inline string — value is already in cellValueBuilder
            return rawValue;
        } else if ("b".equals(currentCellType)) {
            // Boolean
            return "1".equals(rawValue.trim()) ? "true" : "false";
        } else {
            // Number or default — return as-is, but format integers without decimal
            return formatNumericString(rawValue);
        }
    }

    /**
     * Formats a numeric string, removing trailing ".0" for integer values.
     */
    private String formatNumericString(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        try {
            double d = Double.parseDouble(value);
            if (d == Math.floor(d) && !Double.isInfinite(d)) {
                return String.valueOf((long) d);
            }
            return value;
        } catch (NumberFormatException e) {
            return value;
        }
    }

    /**
     * Converts a cell reference (e.g., "B3", "AA1") to a 0-based column index.
     */
    static int cellRefToColumnIndex(String cellRef) {
        if (cellRef == null || cellRef.isEmpty()) {
            return 0;
        }
        int col = 0;
        for (int i = 0; i < cellRef.length(); i++) {
            char c = cellRef.charAt(i);
            if (Character.isLetter(c)) {
                col = col * 26 + (Character.toUpperCase(c) - 'A' + 1);
            } else {
                break;
            }
        }
        return col - 1; // Convert to 0-based
    }
}
