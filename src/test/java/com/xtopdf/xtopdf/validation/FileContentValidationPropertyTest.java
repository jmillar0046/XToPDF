package com.xtopdf.xtopdf.validation;

import com.xtopdf.xtopdf.exceptions.FileConversionException;
import net.jqwik.api.*;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Property-based tests for FileContentValidator.
 *
 * Property 11: Content-Based File Type Validation — For any file whose content bytes
 * do not match the MIME type expected for its declared extension, the FileConversionService
 * SHALL throw a FileConversionException whose message contains both the declared extension
 * and the detected content type.
 *
 * **Validates: Requirements 9.1, 9.2**
 */
class FileContentValidationPropertyTest {

    private final FileContentValidator validator = new FileContentValidator();

    /**
     * Property 11: Content-Based File Type Validation
     *
     * For any file whose content bytes do not match the MIME type expected for its
     * declared extension, the FileContentValidator SHALL throw a FileConversionException
     * whose message contains both the declared extension and the detected content type.
     *
     * **Validates: Requirements 9.1, 9.2**
     */
    @Property(tries = 100)
    @Tag("Feature: repo-efficiency-improvements, Property 11: Content-Based File Type Validation")
    void mismatchedContentAndExtensionThrowsExceptionWithBothInfo(
            @ForAll("mismatchedContentExtensionPairs") ContentExtensionPair pair) {

        MockMultipartFile file = new MockMultipartFile(
                "file", "test" + pair.declaredExtension, "application/octet-stream",
                pair.contentBytes);

        assertThatThrownBy(() -> validator.validate(file, pair.declaredExtension))
                .isInstanceOf(FileConversionException.class)
                .satisfies(ex -> {
                    String message = ex.getMessage();
                    assertThat(message).contains(pair.declaredExtension);
                    assertThat(message).contains(pair.expectedDetectedMime);
                });
    }

    @Provide
    Arbitrary<ContentExtensionPair> mismatchedContentExtensionPairs() {
        // Generate known binary content paired with a WRONG extension
        // Focus on cases where detection IS reliable: PNG, JPEG, GIF
        List<ContentInfo> knownContents = List.of(
                new ContentInfo(
                        new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A},
                        "image/png",
                        ".png"
                ),
                new ContentInfo(
                        new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0},
                        "image/jpeg",
                        ".jpg"
                ),
                new ContentInfo(
                        new byte[]{0x47, 0x49, 0x46, 0x38, 0x39, 0x61},  // GIF89a
                        "image/gif",
                        ".gif"
                )
        );

        // Extensions that are binary-validated (not text-based)
        List<String> binaryExtensions = List.of(
                ".png", ".jpg", ".jpeg", ".gif", ".bmp", ".tiff", ".tif", ".pdf"
        );

        return Arbitraries.of(knownContents).flatMap(contentInfo ->
                Arbitraries.of(binaryExtensions)
                        .filter(ext -> !isMatchingExtension(ext, contentInfo.matchingExtension))
                        .flatMap(wrongExt ->
                                Arbitraries.bytes().array(byte[].class)
                                        .ofMinSize(0).ofMaxSize(50)
                                        .map(randomSuffix -> {
                                            byte[] fullContent = appendBytes(
                                                    contentInfo.magicBytes, randomSuffix);
                                            return new ContentExtensionPair(
                                                    fullContent, wrongExt, contentInfo.mimeType);
                                        })
                        )
        );
    }

    private boolean isMatchingExtension(String ext, String matchingExt) {
        // Handle aliases: .jpg and .jpeg both match JPEG
        if (matchingExt.equals(".jpg")) {
            return ext.equals(".jpg") || ext.equals(".jpeg");
        }
        if (matchingExt.equals(".gif")) {
            return ext.equals(".gif");
        }
        if (matchingExt.equals(".png")) {
            return ext.equals(".png");
        }
        return ext.equals(matchingExt);
    }

    private byte[] appendBytes(byte[] prefix, byte[] suffix) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            bos.write(prefix);
            bos.write(suffix);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bos.toByteArray();
    }

    record ContentInfo(byte[] magicBytes, String mimeType, String matchingExtension) {}

    record ContentExtensionPair(byte[] contentBytes, String declaredExtension,
                                String expectedDetectedMime) {}
}
