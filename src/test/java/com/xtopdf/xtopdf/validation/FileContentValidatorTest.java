package com.xtopdf.xtopdf.validation;

import com.xtopdf.xtopdf.exceptions.FileConversionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for FileContentValidator.
 * Validates: Requirements 9.1, 9.2, 9.3, 9.4
 */
class FileContentValidatorTest {

    private FileContentValidator validator;

    @BeforeEach
    void setUp() {
        validator = new FileContentValidator();
    }

    /**
     * Test that a PNG file with ".png" extension passes validation (no exception).
     * Validates: Requirement 9.1
     */
    @Test
    void pngFileWithPngExtensionPassesValidation() {
        // PNG magic bytes: 0x89 0x50 0x4E 0x47 0x0D 0x0A 0x1A 0x0A
        byte[] pngContent = new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                0x00, 0x00, 0x00, 0x0D  // some additional bytes
        };
        MockMultipartFile file = new MockMultipartFile(
                "file", "image.png", "image/png", pngContent);

        assertThatNoException().isThrownBy(() -> validator.validate(file, ".png"));
    }

    /**
     * Test that a JPEG file with ".png" extension (content mismatch) throws
     * FileConversionException with both the declared extension and detected content type.
     * Validates: Requirements 9.1, 9.2
     */
    @Test
    void jpegFileWithPngExtensionThrowsExceptionWithBothExtensionAndDetectedType() {
        // JPEG magic bytes: 0xFF 0xD8 0xFF
        byte[] jpegContent = new byte[]{
                (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
                0x00, 0x10, 0x4A, 0x46, 0x49, 0x46  // JFIF header
        };
        MockMultipartFile file = new MockMultipartFile(
                "file", "image.png", "image/png", jpegContent);

        assertThatThrownBy(() -> validator.validate(file, ".png"))
                .isInstanceOf(FileConversionException.class)
                .hasMessageContaining(".png")
                .hasMessageContaining("image/jpeg");
    }

    /**
     * Test that indeterminate MIME detection allows conversion to proceed (no exception thrown).
     * Validates: Requirement 9.4
     */
    @Test
    void indeterminateMimeDetectionAllowsConversion() {
        // Random bytes that don't match any known magic bytes
        byte[] unknownContent = new byte[]{
                0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08
        };
        MockMultipartFile file = new MockMultipartFile(
                "file", "document.png", "application/octet-stream", unknownContent);

        assertThatNoException().isThrownBy(() -> validator.validate(file, ".png"));
    }

    /**
     * Test that text-based formats (.csv, .txt, .xml, .json, .md) fall back to
     * extension-only validation (no content check, no exception).
     * Validates: Requirement 9.4
     */
    @Test
    void textBasedFormatsCsvFallBackToExtensionOnlyValidation() {
        // Use PNG magic bytes as content — should still pass because .csv skips content check
        byte[] pngContent = new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
        };
        MockMultipartFile file = new MockMultipartFile(
                "file", "data.csv", "text/csv", pngContent);

        assertThatNoException().isThrownBy(() -> validator.validate(file, ".csv"));
    }

    @Test
    void textBasedFormatsTxtFallBackToExtensionOnlyValidation() {
        byte[] pngContent = new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
        };
        MockMultipartFile file = new MockMultipartFile(
                "file", "data.txt", "text/plain", pngContent);

        assertThatNoException().isThrownBy(() -> validator.validate(file, ".txt"));
    }

    @Test
    void textBasedFormatsXmlFallBackToExtensionOnlyValidation() {
        byte[] pngContent = new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
        };
        MockMultipartFile file = new MockMultipartFile(
                "file", "data.xml", "application/xml", pngContent);

        assertThatNoException().isThrownBy(() -> validator.validate(file, ".xml"));
    }

    @Test
    void textBasedFormatsJsonFallBackToExtensionOnlyValidation() {
        byte[] pngContent = new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
        };
        MockMultipartFile file = new MockMultipartFile(
                "file", "data.json", "application/json", pngContent);

        assertThatNoException().isThrownBy(() -> validator.validate(file, ".json"));
    }

    @Test
    void textBasedFormatsMdFallBackToExtensionOnlyValidation() {
        byte[] pngContent = new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
        };
        MockMultipartFile file = new MockMultipartFile(
                "file", "readme.md", "text/markdown", pngContent);

        assertThatNoException().isThrownBy(() -> validator.validate(file, ".md"));
    }

    /**
     * Test that a GIF file with ".png" extension throws FileConversionException.
     * Validates: Requirements 9.1, 9.2
     */
    @Test
    void gifFileWithPngExtensionThrowsException() {
        // GIF89a magic bytes
        byte[] gifContent = new byte[]{
                0x47, 0x49, 0x46, 0x38, 0x39, 0x61,  // "GIF89a"
                0x01, 0x00, 0x01, 0x00
        };
        MockMultipartFile file = new MockMultipartFile(
                "file", "image.png", "image/png", gifContent);

        assertThatThrownBy(() -> validator.validate(file, ".png"))
                .isInstanceOf(FileConversionException.class)
                .hasMessageContaining(".png")
                .hasMessageContaining("image/gif");
    }

    /**
     * Test that a JPEG file with correct ".jpg" extension passes validation.
     * Validates: Requirement 9.1
     */
    @Test
    void jpegFileWithJpgExtensionPassesValidation() {
        byte[] jpegContent = new byte[]{
                (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
                0x00, 0x10, 0x4A, 0x46, 0x49, 0x46
        };
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", jpegContent);

        assertThatNoException().isThrownBy(() -> validator.validate(file, ".jpg"));
    }

    /**
     * Test that a JPEG file with ".jpeg" alias extension passes validation.
     * Validates: Requirement 9.1
     */
    @Test
    void jpegFileWithJpegExtensionPassesValidation() {
        byte[] jpegContent = new byte[]{
                (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
                0x00, 0x10, 0x4A, 0x46, 0x49, 0x46
        };
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpeg", "image/jpeg", jpegContent);

        assertThatNoException().isThrownBy(() -> validator.validate(file, ".jpeg"));
    }
}
