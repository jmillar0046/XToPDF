package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.EmfToPdfService;
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

class EmfFileConverterTest {

    @Test
    void testConvertToPDF() throws IOException {
        EmfToPdfService emfToPdfService = Mockito.mock(EmfToPdfService.class);
        EmfFileConverter emfFileConverter = new EmfFileConverter(emfToPdfService);
        var outputFile = "output.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.emf", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doNothing().when(emfToPdfService).convertEmfToPdf(any(), any());

        emfFileConverter.convertToPDF(inputFile, outputFile, false);

        verify(emfToPdfService).convertEmfToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        EmfToPdfService emfToPdfService = Mockito.mock(EmfToPdfService.class);
        EmfFileConverter emfFileConverter = new EmfFileConverter(emfToPdfService);
        var outputFile = "output.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.emf", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new IOException("File not found")).when(emfToPdfService).convertEmfToPdf(any(), any());

        assertThrows(RuntimeException.class, () -> emfFileConverter.convertToPDF(inputFile, outputFile, false));
    }
}
