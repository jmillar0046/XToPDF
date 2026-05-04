package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.services.conversion.spreadsheet.DelimiterSeparatedToPdfService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DelimiterSeparatedConverter.
 * Tests CSV and TSV configurations, exception handling, and null validation.
 *
 * Validates: Requirements 5.1, 5.2, 5.3, 5.6
 */
class DelimiterSeparatedConverterTest {

    private final DelimiterSeparatedToPdfService mockService = mock(DelimiterSeparatedToPdfService.class);

    // ---- CSV Configuration Tests ----

    @Test
    void csvConverterUsesCommaDelimiter() throws Exception {
        DelimiterSeparatedConverter csvConverter = new DelimiterSeparatedConverter(
                mockService, ',', "CSV", Set.of(".csv"));

        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "data.csv",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "a,b,c\n1,2,3".getBytes());

        csvConverter.convertToPDF(inputFile, "/tmp/output.pdf");

        verify(mockService).convertDelimiterSeparatedToPdf(any(), any(), eq(','));
    }

    @Test
    void csvConverterReturnsCsvExtension() {
        DelimiterSeparatedConverter csvConverter = new DelimiterSeparatedConverter(
                mockService, ',', "CSV", Set.of(".csv"));

        assertThat(csvConverter.getSupportedExtensions()).isEqualTo(Set.of(".csv"));
    }

    // ---- TSV Configuration Tests ----

    @Test
    void tsvConverterUsesTabDelimiter() throws Exception {
        DelimiterSeparatedConverter tsvConverter = new DelimiterSeparatedConverter(
                mockService, '\t', "TSV", Set.of(".tsv", ".tab"));

        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "data.tsv",
                "text/tab-separated-values",
                "a\tb\tc\n1\t2\t3".getBytes());

        tsvConverter.convertToPDF(inputFile, "/tmp/output.pdf");

        verify(mockService).convertDelimiterSeparatedToPdf(any(), any(), eq('\t'));
    }

    @Test
    void tsvConverterReturnsTsvAndTabExtensions() {
        DelimiterSeparatedConverter tsvConverter = new DelimiterSeparatedConverter(
                mockService, '\t', "TSV", Set.of(".tsv", ".tab"));

        assertThat(tsvConverter.getSupportedExtensions()).isEqualTo(Set.of(".tsv", ".tab"));
    }

    // ---- Exception Handling Tests ----

    @Test
    void ioExceptionIsWrappedInFileConversionExceptionWithFormatName() throws Exception {
        doThrow(new IOException("disk full")).when(mockService)
                .convertDelimiterSeparatedToPdf(any(), any(), eq(','));

        DelimiterSeparatedConverter csvConverter = new DelimiterSeparatedConverter(
                mockService, ',', "CSV", Set.of(".csv"));

        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "data.csv",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "a,b,c".getBytes());

        assertThatThrownBy(() -> csvConverter.convertToPDF(inputFile, "/tmp/output.pdf"))
                .isInstanceOf(FileConversionException.class)
                .hasMessageContaining("CSV")
                .hasMessageContaining("disk full")
                .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void tsvExceptionMessageContainsTsvFormat() throws Exception {
        doThrow(new IOException("stream closed")).when(mockService)
                .convertDelimiterSeparatedToPdf(any(), any(), eq('\t'));

        DelimiterSeparatedConverter tsvConverter = new DelimiterSeparatedConverter(
                mockService, '\t', "TSV", Set.of(".tsv", ".tab"));

        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "data.tsv",
                "text/tab-separated-values",
                "a\tb\tc".getBytes());

        assertThatThrownBy(() -> tsvConverter.convertToPDF(inputFile, "/tmp/output.pdf"))
                .isInstanceOf(FileConversionException.class)
                .hasMessageContaining("TSV")
                .hasMessageContaining("stream closed")
                .hasCauseInstanceOf(IOException.class);
    }

    // ---- Null Validation Tests ----

    @Test
    void nullInputFileThrowsFileConversionException() {
        DelimiterSeparatedConverter converter = new DelimiterSeparatedConverter(
                mockService, ',', "CSV", Set.of(".csv"));

        assertThatThrownBy(() -> converter.convertToPDF(null, "/tmp/output.pdf"))
                .isInstanceOf(FileConversionException.class)
                .hasMessage("Input file must not be null");
    }

    @Test
    void nullOutputFileThrowsFileConversionException() {
        DelimiterSeparatedConverter converter = new DelimiterSeparatedConverter(
                mockService, ',', "CSV", Set.of(".csv"));

        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "data.csv",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "a,b,c".getBytes());

        assertThatThrownBy(() -> converter.convertToPDF(inputFile, null))
                .isInstanceOf(FileConversionException.class)
                .hasMessage("Output file path must not be null");
    }
}
