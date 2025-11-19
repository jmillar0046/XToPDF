package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.WrlToPdfService;
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

class WrlFileConverterTest {

    @Test
    void testConvertToPDF() throws IOException {
        WrlToPdfService wrlToPdfService = Mockito.mock(WrlToPdfService.class);
        WrlFileConverter wrlFileConverter = new WrlFileConverter(wrlToPdfService);
        var outputFile = "output.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.wrl", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doNothing().when(wrlToPdfService).convertWrlToPdf(any(), any());

        wrlFileConverter.convertToPDF(inputFile, outputFile, false);

        verify(wrlToPdfService).convertWrlToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        WrlToPdfService wrlToPdfService = Mockito.mock(WrlToPdfService.class);
        WrlFileConverter wrlFileConverter = new WrlFileConverter(wrlToPdfService);
        var outputFile = "output.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.wrl", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new IOException("File not found")).when(wrlToPdfService).convertWrlToPdf(any(), any());

        assertThrows(RuntimeException.class, () -> wrlFileConverter.convertToPDF(inputFile, outputFile, false));
    }
}
