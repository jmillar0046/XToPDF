package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.IgsToPdfService;
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

class IgsFileConverterTest {

    @Test
    void testConvertToPDF() throws IOException {
        IgsToPdfService igsToPdfService = Mockito.mock(IgsToPdfService.class);
        IgsFileConverter igsFileConverter = new IgsFileConverter(igsToPdfService);
        var outputFile = "output.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.igs", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doNothing().when(igsToPdfService).convertIgsToPdf(any(), any());

        igsFileConverter.convertToPDF(inputFile, outputFile, false);

        verify(igsToPdfService).convertIgsToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        IgsToPdfService igsToPdfService = Mockito.mock(IgsToPdfService.class);
        IgsFileConverter igsFileConverter = new IgsFileConverter(igsToPdfService);
        var outputFile = "output.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.igs", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new IOException("File not found")).when(igsToPdfService).convertIgsToPdf(any(), any());

        assertThrows(RuntimeException.class, () -> igsFileConverter.convertToPDF(inputFile, outputFile, false));
    }
}
