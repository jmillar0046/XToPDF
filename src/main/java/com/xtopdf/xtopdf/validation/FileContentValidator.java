package com.xtopdf.xtopdf.validation;

import com.xtopdf.xtopdf.exceptions.FileConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Map;
import java.util.Set;

/**
 * Validates that file content matches the declared file extension using MIME type detection.
 * <p>
 * Uses {@link URLConnection#guessContentTypeFromStream(InputStream)} supplemented by
 * manual magic byte detection for reliable binary format identification.
 * <p>
 * Text-based formats skip content validation and fall back to extension-only validation,
 * since MIME detection is unreliable for plain text formats.
 * <p>
 * When MIME detection is indeterminate (returns null or application/octet-stream),
 * the validator logs a warning and allows conversion to proceed.
 *
 * @see com.xtopdf.xtopdf.exceptions.FileConversionException
 */
@Component
public class FileContentValidator {

    private static final Logger logger = LoggerFactory.getLogger(FileContentValidator.class);

    /**
     * Text-based extensions that skip content validation entirely.
     * MIME detection is unreliable for these formats.
     */
    private static final Set<String> TEXT_BASED_EXTENSIONS = Set.of(
            ".csv", ".tsv", ".tab", ".txt", ".xml", ".json", ".md", ".markdown",
            ".html", ".svg", ".rtf", ".doc", ".docx", ".xls", ".xlsx",
            ".ppt", ".pptx", ".odt", ".ods", ".odp",
            // CAD/3D formats
            ".dwg", ".dxf", ".dwf", ".dwfx", ".dwt",
            ".iges", ".igs", ".step", ".stp", ".stl", ".obj",
            ".3mf", ".wrl", ".x3d", ".plt", ".hpgl",
            ".emf", ".wmf"
    );

    /**
     * Maps binary file extensions to their expected MIME types.
     * Only binary image formats where magic byte detection is reliable.
     */
    private static final Map<String, Set<String>> EXTENSION_TO_EXPECTED_MIMES = Map.ofEntries(
            Map.entry(".png", Set.of("image/png")),
            Map.entry(".jpg", Set.of("image/jpeg")),
            Map.entry(".jpeg", Set.of("image/jpeg")),
            Map.entry(".gif", Set.of("image/gif")),
            Map.entry(".bmp", Set.of("image/bmp", "image/x-ms-bmp")),
            Map.entry(".tiff", Set.of("image/tiff")),
            Map.entry(".tif", Set.of("image/tiff")),
            Map.entry(".pdf", Set.of("application/pdf"))
    );

    /**
     * Validates that the file content matches the declared extension.
     *
     * @param file      the uploaded file to validate
     * @param extension the declared file extension (e.g., ".png")
     * @throws FileConversionException if the content does not match the declared extension
     */
    public void validate(MultipartFile file, String extension) throws FileConversionException {
        String normalizedExtension = extension.toLowerCase();

        // Text-based formats skip content validation
        if (TEXT_BASED_EXTENSIONS.contains(normalizedExtension)) {
            logger.debug("Skipping content validation for text-based format: {}", normalizedExtension);
            return;
        }

        // Only validate extensions we have MIME mappings for
        Set<String> expectedMimes = EXTENSION_TO_EXPECTED_MIMES.get(normalizedExtension);
        if (expectedMimes == null) {
            logger.debug("No MIME mapping for extension {}, skipping content validation",
                    normalizedExtension);
            return;
        }

        try {
            // Only read the first 16 bytes for magic byte detection — avoids loading entire file into memory
            byte[] header = new byte[16];
            int bytesRead;
            try (InputStream is = file.getInputStream()) {
                bytesRead = is.read(header);
            }
            if (bytesRead < 2) {
                logger.warn("File too small for MIME detection (extension: {}). "
                        + "Allowing conversion with extension-only validation.", normalizedExtension);
                return;
            }

            byte[] content = (bytesRead < header.length)
                    ? java.util.Arrays.copyOf(header, bytesRead)
                    : header;

            String detectedMime = detectMimeType(content);

            if (detectedMime == null || "application/octet-stream".equals(detectedMime)) {
                logger.warn("Indeterminate MIME type for file with extension {}. "
                        + "Allowing conversion with extension-only validation.", normalizedExtension);
                return;
            }

            if (!expectedMimes.contains(detectedMime)) {
                throw new FileConversionException(
                        "File content mismatch: declared extension " + normalizedExtension
                                + " but detected content type " + detectedMime);
            }
        } catch (IOException e) {
            logger.warn("Failed to read file content for MIME validation: {}. "
                    + "Allowing conversion with extension-only validation.", e.getMessage());
        }
    }

    /**
     * Detects the MIME type from file content bytes using both
     * {@link URLConnection#guessContentTypeFromStream} and manual magic byte detection.
     *
     * @param content the file content bytes
     * @return the detected MIME type, or null if indeterminate
     */
    String detectMimeType(byte[] content) {
        if (content == null || content.length < 2) {
            return null;
        }

        // Try magic byte detection first (more reliable for binary formats)
        String magicResult = detectByMagicBytes(content);
        if (magicResult != null) {
            return magicResult;
        }

        // Fall back to URLConnection.guessContentTypeFromStream
        try (InputStream is = new BufferedInputStream(new ByteArrayInputStream(content))) {
            return URLConnection.guessContentTypeFromStream(is);
        } catch (IOException e) {
            logger.debug("URLConnection MIME detection failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Detects MIME type by examining magic bytes at the start of the file content.
     */
    private String detectByMagicBytes(byte[] content) {
        // PNG: 0x89 0x50 0x4E 0x47 (first 4 bytes)
        if (content.length >= 4
                && content[0] == (byte) 0x89
                && content[1] == 0x50
                && content[2] == 0x4E
                && content[3] == 0x47) {
            return "image/png";
        }

        // JPEG: 0xFF 0xD8 0xFF (first 3 bytes)
        if (content.length >= 3
                && content[0] == (byte) 0xFF
                && content[1] == (byte) 0xD8
                && content[2] == (byte) 0xFF) {
            return "image/jpeg";
        }

        // GIF: "GIF87a" or "GIF89a" (first 6 bytes)
        if (content.length >= 6
                && content[0] == 0x47  // G
                && content[1] == 0x49  // I
                && content[2] == 0x46  // F
                && content[3] == 0x38  // 8
                && (content[4] == 0x37 || content[4] == 0x39)  // 7 or 9
                && content[5] == 0x61) {  // a
            return "image/gif";
        }

        // PDF: "%PDF" (first 4 bytes)
        if (content.length >= 4
                && content[0] == 0x25  // %
                && content[1] == 0x50  // P
                && content[2] == 0x44  // D
                && content[3] == 0x46) {  // F
            return "application/pdf";
        }

        // BMP: "BM" (first 2 bytes)
        if (content.length >= 2
                && content[0] == 0x42  // B
                && content[1] == 0x4D) {  // M
            return "image/bmp";
        }

        // TIFF little-endian: 0x49 0x49 0x2A 0x00
        if (content.length >= 4
                && content[0] == 0x49
                && content[1] == 0x49
                && content[2] == 0x2A
                && content[3] == 0x00) {
            return "image/tiff";
        }

        // TIFF big-endian: 0x4D 0x4D 0x00 0x2A
        if (content.length >= 4
                && content[0] == 0x4D
                && content[1] == 0x4D
                && content[2] == 0x00
                && content[3] == 0x2A) {
            return "image/tiff";
        }

        return null;
    }
}
