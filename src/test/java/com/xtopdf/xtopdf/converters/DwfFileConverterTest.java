package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.DwfToPdfService;
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

class DwfFileConverterTest {

    @Test
    void testConvertToPDF() throws IOException {
        DwfToPdfService dwfToPdfService = Mockito.mock(DwfToPdfService.class);
        DwfFileConverter dwfFileConverter = new DwfFileConverter(dwfToPdfService);
        var outputFile = "output.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.dwf", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doNothing().when(dwfToPdfService).convertDwfToPdf(any(), any());

        dwfFileConverter.convertToPDF(inputFile, outputFile, false);

        verify(dwfToPdfService).convertDwfToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        DwfToPdfService dwfToPdfService = Mockito.mock(DwfToPdfService.class);
        DwfFileConverter dwfFileConverter = new DwfFileConverter(dwfToPdfService);
        var outputFile = "output.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.dwf", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new IOException("File not found")).when(dwfToPdfService).convertDwfToPdf(any(), any());

        assertThrows(RuntimeException.class, () -> dwfFileConverter.convertToPDF(inputFile, outputFile, false));
    }
}
