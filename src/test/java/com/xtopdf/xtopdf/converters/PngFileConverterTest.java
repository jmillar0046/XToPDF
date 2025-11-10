package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.PngToPdfService;
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

class PngFileConverterTest {

    @Test
    void testConvertToPDF() throws IOException {
        PngToPdfService pngToPdfService = Mockito.mock(PngToPdfService.class);
        PngFileConverter pngFileConverter = new PngFileConverter(pngToPdfService, Mockito.mock(PageNumberService.class));
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.png", "image/png", "test content".getBytes());

        doNothing().when(pngToPdfService).convertPngToPdf(any(), any());

        pngFileConverter.convertToPDF(inputFile, outputFile);

        verify(pngToPdfService).convertPngToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        PngToPdfService pngToPdfService = Mockito.mock(PngToPdfService.class);
        PngFileConverter pngFileConverter = new PngFileConverter(pngToPdfService, Mockito.mock(PageNumberService.class));
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.png", "image/png", "test content".getBytes());

        doThrow(new IOException("Test exception")).when(pngToPdfService).convertPngToPdf(any(), any());

        assertThrows(RuntimeException.class, () -> pngFileConverter.convertToPDF(inputFile, outputFile));
    }

    @Test
    void testConvertToPDF_NullInputFile_ThrowsNullPointerException() throws IOException {
        PngToPdfService pngToPdfService = Mockito.mock(PngToPdfService.class);
        PngFileConverter pngFileConverter = new PngFileConverter(pngToPdfService, Mockito.mock(PageNumberService.class));
        var outputFile = "outputFile.pdf";

        assertThrows(NullPointerException.class, () -> pngFileConverter.convertToPDF(null, outputFile));
    }

    @Test
    void testConvertToPDF_NullOutputFile_ThrowsNullPointerException() throws IOException {
        PngToPdfService pngToPdfService = Mockito.mock(PngToPdfService.class);
        PngFileConverter pngFileConverter = new PngFileConverter(pngToPdfService, Mockito.mock(PageNumberService.class));
        var inputFile = new MockMultipartFile("inputFile", "test.png", "image/png", "test content".getBytes());

        assertThrows(NullPointerException.class, () -> pngFileConverter.convertToPDF(inputFile, null));
    }
}