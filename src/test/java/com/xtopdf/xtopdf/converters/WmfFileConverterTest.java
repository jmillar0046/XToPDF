package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.WmfToPdfService;
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

class WmfFileConverterTest {

    @Test
    void testConvertToPDF() throws IOException {
        WmfToPdfService wmfToPdfService = Mockito.mock(WmfToPdfService.class);
        WmfFileConverter wmfFileConverter = new WmfFileConverter(wmfToPdfService);
        var outputFile = "output.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.wmf", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doNothing().when(wmfToPdfService).convertWmfToPdf(any(), any());

        wmfFileConverter.convertToPDF(inputFile, outputFile, false);

        verify(wmfToPdfService).convertWmfToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        WmfToPdfService wmfToPdfService = Mockito.mock(WmfToPdfService.class);
        WmfFileConverter wmfFileConverter = new WmfFileConverter(wmfToPdfService);
        var outputFile = "output.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.wmf", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new IOException("File not found")).when(wmfToPdfService).convertWmfToPdf(any(), any());

        assertThrows(RuntimeException.class, () -> wmfFileConverter.convertToPDF(inputFile, outputFile, false));
    }
}
