package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.MarkdownToPdfService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import com.xtopdf.xtopdf.services.PageNumberService;

class MarkdownFileConverterTest {

    @Test
    void testConvertToPDF() throws IOException {
        MarkdownToPdfService markdownToPdfService = Mockito.mock(MarkdownToPdfService.class);
        MarkdownFileConverter markdownFileConverter = new MarkdownFileConverter(markdownToPdfService, Mockito.mock(PageNumberService.class));
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.md", MediaType.TEXT_MARKDOWN_VALUE, "# test content".getBytes());

        doNothing().when(markdownToPdfService).convertMarkdownToPdf(any(), any());

        markdownFileConverter.convertToPDF(inputFile, outputFile);

        verify(markdownToPdfService).convertMarkdownToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        MarkdownToPdfService markdownToPdfService = Mockito.mock(MarkdownToPdfService.class);
        MarkdownFileConverter markdownFileConverter = new MarkdownFileConverter(markdownToPdfService, Mockito.mock(PageNumberService.class));
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.md", MediaType.TEXT_MARKDOWN_VALUE, "# test content".getBytes());

        doThrow(new IOException("File not found")).when(markdownToPdfService).convertMarkdownToPdf(any(), any());

        assertThrows(RuntimeException.class, () -> markdownFileConverter.convertToPDF(inputFile, outputFile));
    }

    @Test
    void testConvertToPDF_NullPointerException_ThrowsNullPointerException() throws IOException {
        MarkdownToPdfService markdownToPdfService = Mockito.mock(MarkdownToPdfService.class);
        MarkdownFileConverter markdownFileConverter = new MarkdownFileConverter(markdownToPdfService, Mockito.mock(PageNumberService.class));
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.md", MediaType.TEXT_MARKDOWN_VALUE, "# test content".getBytes());

        doThrow(new NullPointerException("Null input")).when(markdownToPdfService).convertMarkdownToPdf(any(), any());

        assertThrows(NullPointerException.class, () -> markdownFileConverter.convertToPDF(inputFile, outputFile));
    }
}
