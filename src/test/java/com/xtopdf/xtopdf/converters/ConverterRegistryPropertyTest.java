package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;
import net.jqwik.api.*;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Property-based tests for ConverterRegistry.
 * Tests correctness properties from the design document.
 */
class ConverterRegistryPropertyTest {

    // Known supported extensions for the test registry
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
            ".png", ".jpg", ".jpeg", ".tiff", ".tif", ".md", ".markdown", ".tsv", ".tab", ".csv", ".txt"
    );

    private static ConverterRegistry createTestRegistry() {
        FileConverter pngConverter = createMockConverter(Set.of(".png"));
        FileConverter jpegConverter = createMockConverter(Set.of(".jpg", ".jpeg"));
        FileConverter tiffConverter = createMockConverter(Set.of(".tiff", ".tif"));
        FileConverter markdownConverter = createMockConverter(Set.of(".md", ".markdown"));
        FileConverter tsvConverter = createMockConverter(Set.of(".tsv", ".tab"));
        FileConverter csvConverter = createMockConverter(Set.of(".csv"));
        FileConverter txtConverter = createMockConverter(Set.of(".txt"));

        return new ConverterRegistry(List.of(
                pngConverter, jpegConverter, tiffConverter, markdownConverter,
                tsvConverter, csvConverter, txtConverter
        ));
    }

    /**
     * Property 1: Registry Lookup Correctness — For any supported file extension
     * in the known extension set, the ConverterRegistry SHALL return a non-null
     * FileConverter instance.
     *
     * **Validates: Requirements 1.3**
     */
    @Property(tries = 100)
    @Tag("Feature: repo-efficiency-improvements, Property 1: Registry Lookup Correctness")
    void registryReturnsNonNullConverterForAnySupportedExtension(
            @ForAll("supportedExtensions") String extension) throws FileConversionException {
        ConverterRegistry registry = createTestRegistry();

        FileConverter converter = registry.getConverter(extension);

        assertThat(converter).isNotNull();
    }

    /**
     * Property 2: Unsupported Extension Error — For any string that is not in the
     * set of supported file extensions, the ConverterRegistry SHALL throw a
     * FileConversionException whose message contains the unsupported extension string.
     *
     * **Validates: Requirements 1.4**
     */
    @Property(tries = 100)
    @Tag("Feature: repo-efficiency-improvements, Property 2: Unsupported Extension Error")
    void registryThrowsFileConversionExceptionForUnsupportedExtension(
            @ForAll("unsupportedExtensions") String extension) {
        ConverterRegistry registry = createTestRegistry();

        assertThatThrownBy(() -> registry.getConverter(extension))
                .isInstanceOf(FileConversionException.class)
                .hasMessageContaining(extension);
    }

    @Provide
    Arbitrary<String> supportedExtensions() {
        // Generate random supported extensions, including case variations
        return Arbitraries.of(SUPPORTED_EXTENSIONS.stream().toList())
                .flatMap(ext -> Arbitraries.of(
                        ext,                          // lowercase: ".png"
                        ext.toUpperCase(),             // uppercase: ".PNG"
                        mixCase(ext)                   // mixed case: ".PnG"
                ));
    }

    @Provide
    Arbitrary<String> unsupportedExtensions() {
        // Generate strings that are NOT in the supported set (case-insensitive)
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(10)
                .map(s -> "." + s)
                .filter(s -> !SUPPORTED_EXTENSIONS.contains(s.toLowerCase()));
    }

    private static String mixCase(String ext) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ext.length(); i++) {
            char c = ext.charAt(i);
            sb.append(i % 2 == 0 ? Character.toLowerCase(c) : Character.toUpperCase(c));
        }
        return sb.toString();
    }

    private static FileConverter createMockConverter(Set<String> extensions) {
        return new FileConverter() {
            @Override
            public void convertToPDF(org.springframework.web.multipart.MultipartFile inputFile, String outputFile)
                    throws FileConversionException {
                // no-op for registry tests
            }

            @Override
            public Set<String> getSupportedExtensions() {
                return extensions;
            }
        };
    }
}
