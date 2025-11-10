package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.TiffToPdfService;
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

class TiffFileConverterTest {

    @Test
    void testConvertToPDF() throws IOException {
        TiffToPdfService tiffToPdfService = Mockito.mock(TiffToPdfService.class);
        TiffFileConverter tiffFileConverter = new TiffFileConverter(tiffToPdfService, Mockito.mock(PageNumberService.class));
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.tiff", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doNothing().when(tiffToPdfService).convertTiffToPdf(any(), any());

        tiffFileConverter.convertToPDF(inputFile, outputFile);

        verify(tiffToPdfService).convertTiffToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        TiffToPdfService tiffToPdfService = Mockito.mock(TiffToPdfService.class);
        TiffFileConverter tiffFileConverter = new TiffFileConverter(tiffToPdfService, Mockito.mock(PageNumberService.class));
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.tiff", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new IOException("File processing error")).when(tiffToPdfService).convertTiffToPdf(any(), any());

        assertThrows(RuntimeException.class, () -> tiffFileConverter.convertToPDF(inputFile, outputFile));
    }
}