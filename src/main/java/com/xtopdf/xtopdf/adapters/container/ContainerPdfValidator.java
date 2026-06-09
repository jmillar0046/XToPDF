package com.xtopdf.xtopdf.adapters.container;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

/**
 * Utility class for validating PDF response bytes from container conversion endpoints.
 * Ensures the container actually returned valid PDF content before writing to disk.
 */
public final class ContainerPdfValidator {

    private static final byte[] PDF_MAGIC = {0x25, 0x50, 0x44, 0x46}; // %PDF

    private ContainerPdfValidator() {
        // Utility class — no instantiation
    }

    /**
     * Validates that the response bytes from a container conversion endpoint
     * represent a valid PDF (starting with %PDF magic bytes).
     *
     * @param responseBytes the raw bytes returned by the container's conversion endpoint
     * @throws FileConversionException if the response is null, too short, or does not begin with %PDF
     */
    public static void validatePdfResponse(byte[] responseBytes) throws FileConversionException {
        if (responseBytes == null || responseBytes.length < 4) {
            throw new FileConversionException("Container returned empty or invalid response");
        }
        if (responseBytes[0] != PDF_MAGIC[0] || responseBytes[1] != PDF_MAGIC[1]
                || responseBytes[2] != PDF_MAGIC[2] || responseBytes[3] != PDF_MAGIC[3]) {
            throw new FileConversionException("Container conversion output is not a valid PDF");
        }
    }
}
