package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.services.conversion.document.DocxToPdfService;
import net.jqwik.api.*;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

/**
 * Property-based tests for DocxFileConverter exception wrapping correctness.
 *
 * <p><b>Property 9: Exception wrapping correctness</b></p>
 * <p>For any IOException thrown by DocxToPdfService.convertDocxToPdf(), the DocxFileConverter
 * SHALL wrap it in a FileConversionException whose message contains the substring "DOCX"
 * and whose cause is the original IOException.</p>
 *
 * <p><b>Validates: Requirements 7.1, 7.3</b></p>
 */
class DocxFileConverterExceptionPropertyTest {

    /**
     * Property 9: Exception wrapping correctness
     *
     * For any IOException message, DocxFileConverter wraps it in a FileConversionException
     * whose message contains "DOCX" and whose cause is the original IOException.
     *
     * Validates: Requirements 7.1, 7.3
     */
    @Property(tries = 100)
    @Label("IOException is wrapped in FileConversionException with DOCX in message and original cause preserved")
    void ioExceptionIsWrappedInFileConversionException(
            @ForAll("ioExceptionMessages") String ioMessage) throws IOException {

        // Arrange
        DocxToPdfService mockService = Mockito.mock(DocxToPdfService.class);
        DocxFileConverter converter = new DocxFileConverter(mockService);

        IOException originalException = new IOException(ioMessage);
        doThrow(originalException).when(mockService).convertDocxToPdf(any(), any());

        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "test.docx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "test content".getBytes());

        // Act
        Throwable thrown = catchThrowable(() -> converter.convertToPDF(inputFile, "output.pdf"));

        // Assert: must be FileConversionException
        assertThat(thrown)
                .isInstanceOf(FileConversionException.class)
                .hasMessageContaining("DOCX");

        // Assert: cause is the original IOException
        assertThat(thrown.getCause())
                .isSameAs(originalException);
    }

    @Provide
    Arbitrary<String> ioExceptionMessages() {
        return Arbitraries.oneOf(
                Arbitraries.of(
                        "File not found",
                        "Permission denied",
                        "Disk full",
                        "Stream closed",
                        "Connection reset",
                        "Broken pipe",
                        "Error processing DOCX file",
                        "Corrupt ZIP entry"
                ),
                Arbitraries.strings()
                        .withCharRange('a', 'z')
                        .ofMinLength(1)
                        .ofMaxLength(50)
        );
    }
}
