package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.PltToPdfService;
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

class PltFileConverterTest {

    @Test
    void testConvertToPDF() throws IOException {
        PltToPdfService pltToPdfService = Mockito.mock(PltToPdfService.class);
        PltFileConverter pltFileConverter = new PltFileConverter(pltToPdfService);
        var outputFile = "output.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.plt", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doNothing().when(pltToPdfService).convertPltToPdf(any(), any());

        pltFileConverter.convertToPDF(inputFile, outputFile, false);

        verify(pltToPdfService).convertPltToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        PltToPdfService pltToPdfService = Mockito.mock(PltToPdfService.class);
        PltFileConverter pltFileConverter = new PltFileConverter(pltToPdfService);
        var outputFile = "output.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.plt", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new IOException("File not found")).when(pltToPdfService).convertPltToPdf(any(), any());

        assertThrows(RuntimeException.class, () -> pltFileConverter.convertToPDF(inputFile, outputFile, false));
    }
}
