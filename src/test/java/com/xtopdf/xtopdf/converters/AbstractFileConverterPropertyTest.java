package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;
import net.jqwik.api.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Property-based tests for AbstractFileConverter.
 *
 * Property 3: AbstractFileConverter null guard — For any subclass of AbstractFileConverter,
 * calling convertToPDF(null, validPath) or convertToPDF(validFile, null) throws
 * FileConversionException with message containing "must not be null", and doConvert is never called.
 *
 * Property 4: AbstractFileConverter exception wrapping — For any exception type thrown by
 * doConvert (except FileConversionException itself), convertToPDF wraps it in
 * FileConversionException with message matching "Error converting {FORMAT} to PDF: {message}".
 * FileConversionException thrown by doConvert passes through unwrapped.
 *
 * **Validates: Requirements 8.2, 8.3**
 */
class AbstractFileConverterPropertyTest {

    /**
     * Property 3: AbstractFileConverter null guard — null inputFile
     *
     * For any subclass of AbstractFileConverter, calling convertToPDF(null, validPath)
     * throws FileConversionException with message containing "must not be null",
     * and the protected doConvert() method is NOT invoked.
     *
     * **Validates: Requirements 8.2**
     */
    @Property(tries = 25)
    @Tag("Feature: codebase-hardening, Property 3: AbstractFileConverter null guard")
    void nullInputFileThrowsExceptionAndDoConvertNotCalled(
            @ForAll("validOutputPaths") String outputPath) {

        var doConvertCalled = new AtomicBoolean(false);
        var converter = createTestConverter("TestFormat", doConvertCalled, null);

        assertThatThrownBy(() -> converter.convertToPDF(null, outputPath))
                .isInstanceOf(FileConversionException.class)
                .satisfies(ex -> assertThat(ex.getMessage()).contains("must not be null"));

        assertThat(doConvertCalled.get())
                .as("doConvert should NOT be invoked when inputFile is null")
                .isFalse();
    }

    /**
     * Property 3: AbstractFileConverter null guard — null outputFile
     *
     * For any subclass of AbstractFileConverter, calling convertToPDF(validFile, null)
     * throws FileConversionException with message containing "must not be null",
     * and the protected doConvert() method is NOT invoked.
     *
     * **Validates: Requirements 8.2**
     */
    @Property(tries = 25)
    @Tag("Feature: codebase-hardening, Property 3: AbstractFileConverter null guard")
    void nullOutputFileThrowsExceptionAndDoConvertNotCalled(
            @ForAll("validMultipartFiles") MockMultipartFile inputFile) {

        var doConvertCalled = new AtomicBoolean(false);
        var converter = createTestConverter("TestFormat", doConvertCalled, null);

        assertThatThrownBy(() -> converter.convertToPDF(inputFile, null))
                .isInstanceOf(FileConversionException.class)
                .satisfies(ex -> assertThat(ex.getMessage()).contains("must not be null"));

        assertThat(doConvertCalled.get())
                .as("doConvert should NOT be invoked when outputFile is null")
                .isFalse();
    }

    /**
     * Property 4: AbstractFileConverter exception wrapping — non-FileConversionException
     *
     * For any exception type thrown by doConvert (except FileConversionException itself),
     * convertToPDF wraps it in FileConversionException with message matching
     * "Error converting {FORMAT} to PDF: {message}".
     *
     * **Validates: Requirements 8.3**
     */
    @Property(tries = 25)
    @Tag("Feature: codebase-hardening, Property 4: AbstractFileConverter exception wrapping")
    void nonFileConversionExceptionIsWrapped(
            @ForAll("wrappableExceptions") Exception exceptionToThrow,
            @ForAll("formatNames") String formatName) {

        var doConvertCalled = new AtomicBoolean(false);
        var converter = createTestConverter(formatName, doConvertCalled, exceptionToThrow);
        var inputFile = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());

        assertThatThrownBy(() -> converter.convertToPDF(inputFile, "/tmp/output.pdf"))
                .isInstanceOf(FileConversionException.class)
                .satisfies(ex -> {
                    String expectedMessage = "Error converting " + formatName + " to PDF: " + exceptionToThrow.getMessage();
                    assertThat(ex.getMessage()).isEqualTo(expectedMessage);
                    assertThat(((FileConversionException) ex).getCause()).isSameAs(exceptionToThrow);
                });

        assertThat(doConvertCalled.get())
                .as("doConvert should be invoked before exception wrapping")
                .isTrue();
    }

    /**
     * Property 4: AbstractFileConverter exception wrapping — FileConversionException passthrough
     *
     * FileConversionException thrown by doConvert passes through unwrapped.
     *
     * **Validates: Requirements 8.3**
     */
    @Property(tries = 25)
    @Tag("Feature: codebase-hardening, Property 4: AbstractFileConverter exception wrapping")
    void fileConversionExceptionPassesThroughUnwrapped(
            @ForAll("fileConversionExceptionMessages") String message,
            @ForAll("formatNames") String formatName) {

        var originalException = new FileConversionException(message);
        var doConvertCalled = new AtomicBoolean(false);
        var converter = createTestConverter(formatName, doConvertCalled, originalException);
        var inputFile = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());

        assertThatThrownBy(() -> converter.convertToPDF(inputFile, "/tmp/output.pdf"))
                .isInstanceOf(FileConversionException.class)
                .isSameAs(originalException)
                .satisfies(ex -> {
                    // Verify it's the same exception, not wrapped
                    assertThat(ex.getMessage()).isEqualTo(message);
                    assertThat(ex.getCause()).isNull();
                });

        assertThat(doConvertCalled.get())
                .as("doConvert should be invoked")
                .isTrue();
    }

    // --- Providers ---

    @Provide
    Arbitrary<String> validOutputPaths() {
        return Arbitraries.of(
                "/tmp/output.pdf",
                "/home/user/documents/result.pdf",
                "/var/data/converted.pdf",
                "output.pdf",
                "./build/test.pdf"
        );
    }

    @Provide
    Arbitrary<MockMultipartFile> validMultipartFiles() {
        return Arbitraries.of(
                new MockMultipartFile("file", "test.txt", "text/plain", "hello".getBytes()),
                new MockMultipartFile("file", "doc.docx", "application/octet-stream", "content".getBytes()),
                new MockMultipartFile("file", "image.png", "image/png", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47}),
                new MockMultipartFile("file", "data.csv", "text/csv", "a,b,c".getBytes()),
                new MockMultipartFile("file", "report.html", "text/html", "<html></html>".getBytes())
        );
    }

    @Provide
    Arbitrary<Exception> wrappableExceptions() {
        return Arbitraries.of(
                new RuntimeException("something went wrong"),
                new IllegalArgumentException("bad argument"),
                new IOException("file not found"),
                new NullPointerException("unexpected null"),
                new UnsupportedOperationException("not supported"),
                new IllegalStateException("invalid state"),
                new ArrayIndexOutOfBoundsException("index 5 out of bounds")
        );
    }

    @Provide
    Arbitrary<String> formatNames() {
        return Arbitraries.of(
                "DOCX", "HTML", "CSV", "PNG", "SVG", "XLSX", "MD", "BMP", "JSON", "TXT"
        );
    }

    @Provide
    Arbitrary<String> fileConversionExceptionMessages() {
        return Arbitraries.of(
                "Unsupported file format",
                "Input file must not be null",
                "Conversion failed due to corrupt input",
                "Cannot process empty file",
                "File format mismatch detected"
        );
    }

    // --- Test helper: concrete subclass of AbstractFileConverter ---

    private AbstractFileConverter createTestConverter(
            String formatName,
            AtomicBoolean doConvertCalled,
            Exception exceptionToThrow) {

        return new AbstractFileConverter() {
            @Override
            protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
                doConvertCalled.set(true);
                if (exceptionToThrow != null) {
                    throw exceptionToThrow;
                }
            }

            @Override
            protected String getFormatName() {
                return formatName;
            }

            @Override
            public Set<String> getSupportedExtensions() {
                return Set.of(".test");
            }
        };
    }
}
