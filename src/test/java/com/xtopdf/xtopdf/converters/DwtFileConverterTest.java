package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.DwtToPdfService;
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

class DwtFileConverterTest {

    @Test
    void testConvertToPDF() throws IOException {
        DwtToPdfService dwtToPdfService = Mockito.mock(DwtToPdfService.class);
        DwtFileConverter dwtFileConverter = new DwtFileConverter(dwtToPdfService);
        var outputFile = "output.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.dwt", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doNothing().when(dwtToPdfService).convertDwtToPdf(any(), any());

        dwtFileConverter.convertToPDF(inputFile, outputFile, false);

        verify(dwtToPdfService).convertDwtToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        DwtToPdfService dwtToPdfService = Mockito.mock(DwtToPdfService.class);
        DwtFileConverter dwtFileConverter = new DwtFileConverter(dwtToPdfService);
        var outputFile = "output.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.dwt", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new IOException("File not found")).when(dwtToPdfService).convertDwtToPdf(any(), any());

        assertThrows(RuntimeException.class, () -> dwtFileConverter.convertToPDF(inputFile, outputFile, false));
    }
}
