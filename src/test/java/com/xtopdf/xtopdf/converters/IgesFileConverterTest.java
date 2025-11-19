package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.IgesToPdfService;
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

class IgesFileConverterTest {

    @Test
    void testConvertToPDF() throws IOException {
        IgesToPdfService igesToPdfService = Mockito.mock(IgesToPdfService.class);
        IgesFileConverter igesFileConverter = new IgesFileConverter(igesToPdfService);
        var outputFile = "output.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.iges", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doNothing().when(igesToPdfService).convertIgesToPdf(any(), any());

        igesFileConverter.convertToPDF(inputFile, outputFile, false);

        verify(igesToPdfService).convertIgesToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        IgesToPdfService igesToPdfService = Mockito.mock(IgesToPdfService.class);
        IgesFileConverter igesFileConverter = new IgesFileConverter(igesToPdfService);
        var outputFile = "output.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.iges", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new IOException("File not found")).when(igesToPdfService).convertIgesToPdf(any(), any());

        assertThrows(RuntimeException.class, () -> igesFileConverter.convertToPDF(inputFile, outputFile, false));
    }
}
