package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.StlToPdfService;
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

class StlFileConverterTest {

    @Test
    void testConvertToPDF() throws IOException {
        StlToPdfService stlToPdfService = Mockito.mock(StlToPdfService.class);
        StlFileConverter stlFileConverter = new StlFileConverter(stlToPdfService);
        var outputFile = "output.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.stl", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doNothing().when(stlToPdfService).convertStlToPdf(any(), any());

        stlFileConverter.convertToPDF(inputFile, outputFile, false);

        verify(stlToPdfService).convertStlToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        StlToPdfService stlToPdfService = Mockito.mock(StlToPdfService.class);
        StlFileConverter stlFileConverter = new StlFileConverter(stlToPdfService);
        var outputFile = "output.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.stl", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new IOException("File not found")).when(stlToPdfService).convertStlToPdf(any(), any());

        assertThrows(RuntimeException.class, () -> stlFileConverter.convertToPDF(inputFile, outputFile, false));
    }
}
