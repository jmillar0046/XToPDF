package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.converters.ConverterRegistry;
import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.dto.ConversionParameters;
import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.validation.FileContentValidator;
import net.jqwik.api.*;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for file extension extraction in FileConversionService.
 *
 * Property 9: File extension extraction correctness
 * - For any filename with dot only at index 0 (e.g., ".gitignore", ".env") → throws FileConversionException
 * - For any filename with no dot → throws FileConversionException
 * - For any filename with a valid extension (dot not at index 0, e.g., "file.pdf", "my.file.docx")
 *   → returns lowercase substring from last dot (e.g., ".pdf", ".docx")
 * - The returned extension always starts with '.' and is lowercase
 *
 * **Validates: Requirements 22.1, 22.2, 22.3**
 */
class ExtractExtensionPropertyTest {

    private Method extractExtensionMethod;

    ExtractExtensionPropertyTest() throws NoSuchMethodException {
        extractExtensionMethod = FileConversionService.class.getDeclaredMethod("extractExtension", String.class);
        extractExtensionMethod.setAccessible(true);
    }

    private FileConversionService createService() {
        var converterRegistry = mock(ConverterRegistry.class);
        var contentValidator = mock(FileContentValidator.class);
        var pdfMergeService = mock(com.xtopdf.xtopdf.services.operations.PdfMergeService.class);
        var pageNumberService = mock(com.xtopdf.xtopdf.services.operations.PageNumberService.class);
        var watermarkService = mock(com.xtopdf.xtopdf.services.operations.WatermarkService.class);
        var containerOrchestrationService = mock(com.xtopdf.xtopdf.services.orchestration.ContainerOrchestrationService.class);
        return new FileConversionService(converterRegistry, contentValidator, pdfMergeService, pageNumberService, watermarkService, containerOrchestrationService,
                new com.xtopdf.xtopdf.config.MetricsConfiguration.ConversionMetrics(
                        new io.micrometer.core.instrument.simple.SimpleMeterRegistry()), 300);
    }

    private String invokeExtractExtension(FileConversionService service, String fileName) throws Throwable {
        try {
            return (String) extractExtensionMethod.invoke(service, fileName);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    /**
     * Property 9: Dot-files (dot only at index 0) should throw FileConversionException.
     *
     * For any filename with a dot only at index 0 and no subsequent dots (e.g., ".gitignore", ".env"),
     * extractExtension SHALL throw FileConversionException with "Unsupported file format".
     *
     * **Validates: Requirements 22.1**
     */
    @Property(tries = 25)
    @Tag("Feature: codebase-hardening, Property 9: File extension extraction correctness")
    void dotFilesThrowFileConversionException(@ForAll("dotFiles") String fileName) {
        var service = createService();

        assertThatThrownBy(() -> invokeExtractExtension(service, fileName))
                .isInstanceOf(FileConversionException.class)
                .hasMessageContaining("Unsupported file format");
    }

    /**
     * Property 9: Filenames with no dot should throw FileConversionException.
     *
     * For any filename containing no dot character, extractExtension SHALL throw
     * FileConversionException with "Unsupported file format".
     *
     * **Validates: Requirements 22.1**
     */
    @Property(tries = 25)
    @Tag("Feature: codebase-hardening, Property 9: File extension extraction correctness")
    void noDotFilesThrowFileConversionException(@ForAll("noDotFiles") String fileName) {
        var service = createService();

        assertThatThrownBy(() -> invokeExtractExtension(service, fileName))
                .isInstanceOf(FileConversionException.class)
                .hasMessageContaining("Unsupported file format");
    }

    /**
     * Property 9: Valid filenames return lowercase extension starting with dot.
     *
     * For any filename with a valid extension (dot not at index 0), extractExtension
     * SHALL return the lowercase substring starting from the last dot.
     * The returned extension always starts with '.' and is lowercase.
     *
     * **Validates: Requirements 22.2, 22.3**
     */
    @Property(tries = 25)
    @Tag("Feature: codebase-hardening, Property 9: File extension extraction correctness")
    void validFilenamesReturnLowercaseExtensionFromLastDot(@ForAll("validFilenames") String fileName) throws Throwable {
        var service = createService();

        String result = invokeExtractExtension(service, fileName);

        // Result always starts with '.'
        assertThat(result)
                .as("Extension should start with '.'")
                .startsWith(".");

        // Result is always lowercase
        assertThat(result)
                .as("Extension should be lowercase")
                .isEqualTo(result.toLowerCase());

        // Result matches the expected extension from last dot
        String lowerFileName = fileName.toLowerCase();
        int lastDot = lowerFileName.lastIndexOf('.');
        String expectedExtension = lowerFileName.substring(lastDot);
        assertThat(result)
                .as("Extension should be substring from last dot of lowercase filename")
                .isEqualTo(expectedExtension);
    }

    // --- Providers ---

    @Provide
    Arbitrary<String> dotFiles() {
        return Arbitraries.of(
                ".gitignore",
                ".env",
                ".config",
                ".bashrc",
                ".dockerignore",
                ".editorconfig",
                ".eslintrc",
                ".npmrc",
                ".profile",
                ".htaccess"
        );
    }

    @Provide
    Arbitrary<String> noDotFiles() {
        return Arbitraries.of(
                "readme",
                "LICENSE",
                "Makefile",
                "Dockerfile",
                "Jenkinsfile",
                "CHANGELOG",
                "NOTICE",
                "TODO",
                "CONTRIBUTING",
                "gradlew"
        );
    }

    @Provide
    Arbitrary<String> validFilenames() {
        return Arbitraries.of(
                "report.pdf",
                "image.PNG",
                "my.file.docx",
                "archive.tar.gz",
                "document.XLSX",
                "photo.JpEg",
                "data.csv",
                "presentation.pptx",
                "notes.TXT",
                "drawing.svg",
                "multi.dot.name.html",
                "CamelCase.Pdf",
                "UPPER.BMP",
                "test-file.md"
        );
    }
}
