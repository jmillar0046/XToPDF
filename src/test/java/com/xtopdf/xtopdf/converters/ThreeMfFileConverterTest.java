package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.ThreeMfToPdfService;
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

class ThreeMfFileConverterTest {

    @Test
    void testConvertToPDF() throws IOException {
        ThreeMfToPdfService threeMfToPdfService = Mockito.mock(ThreeMfToPdfService.class);
        ThreeMfFileConverter threeMfFileConverter = new ThreeMfFileConverter(threeMfToPdfService);
        var outputFile = "output.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.3mf", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doNothing().when(threeMfToPdfService).convert3mfToPdf(any(), any());

        threeMfFileConverter.convertToPDF(inputFile, outputFile, false);

        verify(threeMfToPdfService).convert3mfToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        ThreeMfToPdfService threeMfToPdfService = Mockito.mock(ThreeMfToPdfService.class);
        ThreeMfFileConverter threeMfFileConverter = new ThreeMfFileConverter(threeMfToPdfService);
        var outputFile = "output.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.3mf", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new IOException("File not found")).when(threeMfToPdfService).convert3mfToPdf(any(), any());

        assertThrows(RuntimeException.class, () -> threeMfFileConverter.convertToPDF(inputFile, outputFile, false));
    }
}
