package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.GifToPdfService;
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

class GifFileConverterTest {

    @Test
    void testConvertToPDF() throws IOException {
        GifToPdfService gifToPdfService = Mockito.mock(GifToPdfService.class);
        GifFileConverter gifFileConverter = new GifFileConverter(gifToPdfService);
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.gif", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doNothing().when(gifToPdfService).convertGifToPdf(any(), any());

        gifFileConverter.convertToPDF(inputFile, outputFile);

        verify(gifToPdfService).convertGifToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        GifToPdfService gifToPdfService = Mockito.mock(GifToPdfService.class);
        GifFileConverter gifFileConverter = new GifFileConverter(gifToPdfService);
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.gif", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new IOException("File not found")).when(gifToPdfService).convertGifToPdf(any(), any());

        assertThrows(RuntimeException.class, () -> gifFileConverter.convertToPDF(inputFile, outputFile));
    }

    @Test
    void testConvertToPDF_NullPointerException_ThrowsNullPointerException() throws IOException {
        GifToPdfService gifToPdfService = Mockito.mock(GifToPdfService.class);
        GifFileConverter gifFileConverter = new GifFileConverter(gifToPdfService);
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.gif", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new NullPointerException("Null input")).when(gifToPdfService).convertGifToPdf(any(), any());

        assertThrows(NullPointerException.class, () -> gifFileConverter.convertToPDF(inputFile, outputFile));
    }
}
