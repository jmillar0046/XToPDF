package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.StpToPdfService;
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

class StpFileConverterTest {

    @Test
    void testConvertToPDF() throws IOException {
        StpToPdfService stpToPdfService = Mockito.mock(StpToPdfService.class);
        StpFileConverter stpFileConverter = new StpFileConverter(stpToPdfService);
        var outputFile = "output.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.stp", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doNothing().when(stpToPdfService).convertStpToPdf(any(), any());

        stpFileConverter.convertToPDF(inputFile, outputFile, false);

        verify(stpToPdfService).convertStpToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        StpToPdfService stpToPdfService = Mockito.mock(StpToPdfService.class);
        StpFileConverter stpFileConverter = new StpFileConverter(stpToPdfService);
        var outputFile = "output.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.stp", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new IOException("File not found")).when(stpToPdfService).convertStpToPdf(any(), any());

        assertThrows(RuntimeException.class, () -> stpFileConverter.convertToPDF(inputFile, outputFile, false));
    }
}
