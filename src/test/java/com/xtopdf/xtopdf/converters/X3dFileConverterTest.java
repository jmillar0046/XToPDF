package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.X3dToPdfService;
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

class X3dFileConverterTest {

    @Test
    void testConvertToPDF() throws IOException {
        X3dToPdfService x3dToPdfService = Mockito.mock(X3dToPdfService.class);
        X3dFileConverter x3dFileConverter = new X3dFileConverter(x3dToPdfService);
        var outputFile = "output.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.x3d", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doNothing().when(x3dToPdfService).convertX3dToPdf(any(), any());

        x3dFileConverter.convertToPDF(inputFile, outputFile, false);

        verify(x3dToPdfService).convertX3dToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        X3dToPdfService x3dToPdfService = Mockito.mock(X3dToPdfService.class);
        X3dFileConverter x3dFileConverter = new X3dFileConverter(x3dToPdfService);
        var outputFile = "output.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.x3d", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new IOException("File not found")).when(x3dToPdfService).convertX3dToPdf(any(), any());

        assertThrows(RuntimeException.class, () -> x3dFileConverter.convertToPDF(inputFile, outputFile, false));
    }
}
