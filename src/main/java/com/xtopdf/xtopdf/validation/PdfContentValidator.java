package com.xtopdf.xtopdf.validation;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Utility class for validating that a file contains valid PDF content
 * by checking for the PDF magic bytes (%PDF) at the start of the file.
 *
 * <p>Used by PdfOperationsController to reject non-PDF uploads early
 * before attempting merge, page-number, or watermark operations.</p>
 */
public final class PdfContentValidator {

    private static final byte[] PDF_MAGIC = {0x25, 0x50, 0x44, 0x46}; // %PDF

    private PdfContentValidator() {
        // Utility class — prevent instantiation
    }

    /**
     * Checks whether the given file starts with the PDF magic bytes (%PDF).
     *
     * @param file the uploaded multipart file to validate
     * @return true if the file starts with the PDF magic bytes, false otherwise
     * @throws IOException if an I/O error occurs reading the file
     */
    public static boolean isPdf(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return false;
        byte[] header = new byte[4];
        try (InputStream is = file.getInputStream()) {
            int read = is.read(header);
            if (read < 4) return false;
        }
        return Arrays.equals(header, PDF_MAGIC);
    }
}
