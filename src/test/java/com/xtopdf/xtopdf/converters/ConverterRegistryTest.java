package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Unit tests for ConverterRegistry.
 * Validates: Requirements 1.1, 1.3, 1.4, 1.5, 1.7
 */
class ConverterRegistryTest {

    private FileConverter pngConverter;
    private FileConverter jpegConverter;
    private FileConverter tiffConverter;
    private FileConverter markdownConverter;
    private FileConverter tsvConverter;
    private ConverterRegistry registry;

    @BeforeEach
    void setUp() {
        pngConverter = createMockConverter(Set.of(".png"));
        jpegConverter = createMockConverter(Set.of(".jpg", ".jpeg"));
        tiffConverter = createMockConverter(Set.of(".tiff", ".tif"));
        markdownConverter = createMockConverter(Set.of(".md", ".markdown"));
        tsvConverter = createMockConverter(Set.of(".tsv", ".tab"));

        registry = new ConverterRegistry(List.of(
                pngConverter, jpegConverter, tiffConverter, markdownConverter, tsvConverter
        ));
    }

    @Test
    void getConverterReturnsCorrectConverterForKnownExtension() throws FileConversionException {
        assertThat(registry.getConverter(".png")).isSameAs(pngConverter);
        assertThat(registry.getConverter(".jpg")).isSameAs(jpegConverter);
        assertThat(registry.getConverter(".tiff")).isSameAs(tiffConverter);
        assertThat(registry.getConverter(".md")).isSameAs(markdownConverter);
        assertThat(registry.getConverter(".tsv")).isSameAs(tsvConverter);
    }

    @Test
    void aliasExtensionsResolveToSameConverterInstance() throws FileConversionException {
        // .jpg and .jpeg → same JpegFileConverter
        assertThat(registry.getConverter(".jpg")).isSameAs(registry.getConverter(".jpeg"));

        // .tif and .tiff → same TiffFileConverter
        assertThat(registry.getConverter(".tif")).isSameAs(registry.getConverter(".tiff"));

        // .md and .markdown → same MarkdownFileConverter
        assertThat(registry.getConverter(".md")).isSameAs(registry.getConverter(".markdown"));

        // .tsv and .tab → same TsvFileConverter
        assertThat(registry.getConverter(".tsv")).isSameAs(registry.getConverter(".tab"));
    }

    @Test
    void getConverterThrowsFileConversionExceptionForUnsupportedExtension() {
        assertThatThrownBy(() -> registry.getConverter(".xyz"))
                .isInstanceOf(FileConversionException.class)
                .hasMessageContaining(".xyz");
    }

    @Test
    void getConverterThrowsFileConversionExceptionForEmptyString() {
        assertThatThrownBy(() -> registry.getConverter(""))
                .isInstanceOf(FileConversionException.class);
    }

    @Test
    void getSupportedExtensionsReturnsUnmodifiableSet() {
        Set<String> extensions = registry.getSupportedExtensions();

        assertThatThrownBy(() -> extensions.add(".newext"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void getSupportedExtensionsContainsAllRegisteredExtensions() {
        Set<String> extensions = registry.getSupportedExtensions();

        assertThat(extensions).containsExactlyInAnyOrder(
                ".png", ".jpg", ".jpeg", ".tiff", ".tif", ".md", ".markdown", ".tsv", ".tab"
        );
    }

    @Test
    void registryHandlesCaseInsensitiveLookups() throws FileConversionException {
        assertThat(registry.getConverter(".PNG")).isSameAs(pngConverter);
        assertThat(registry.getConverter(".Jpg")).isSameAs(jpegConverter);
        assertThat(registry.getConverter(".TIFF")).isSameAs(tiffConverter);
        assertThat(registry.getConverter(".MD")).isSameAs(markdownConverter);
        assertThat(registry.getConverter(".TSV")).isSameAs(tsvConverter);
    }

    @Test
    void registryHandlesMixedCaseExtensions() throws FileConversionException {
        assertThat(registry.getConverter(".PnG")).isSameAs(pngConverter);
        assertThat(registry.getConverter(".JpEg")).isSameAs(jpegConverter);
        assertThat(registry.getConverter(".TaB")).isSameAs(tsvConverter);
    }

    /**
     * Creates a simple FileConverter stub that returns the given extensions.
     * The convertToPDF method is a no-op since we only test registry behavior.
     */
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
