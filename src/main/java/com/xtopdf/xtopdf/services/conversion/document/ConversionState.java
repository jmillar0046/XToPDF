package com.xtopdf.xtopdf.services.conversion.document;

import java.util.HashMap;
import java.util.Map;

/**
 * Mutable state scoped to a single DOCX-to-PDF conversion invocation.
 * Passed through the method chain instead of multiple primitive parameters.
 */
final class ConversionState {

    private final Map<String, Integer> listCounters = new HashMap<>();
    private boolean contentRendered = false;

    boolean hasContentBeenRendered() {
        return contentRendered;
    }

    void markContentRendered() {
        contentRendered = true;
    }

    int nextListNumber(String numId) {
        int counter = listCounters.getOrDefault(numId, 0) + 1;
        listCounters.put(numId, counter);
        return counter;
    }
}
