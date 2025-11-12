package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.DwgToPdfService;
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

class DwgFileConverterTest {

    @Test
    void testConvertToPDF() throws IOException {
        DwgToPdfService dwgToPdfService = Mockito.mock(DwgToPdfService.class);
        DwgFileConverter dwgFileConverter = new DwgFileConverter(dwgToPdfService);
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.dwg", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doNothing().when(dwgToPdfService).convertDwgToPdf(any(), any());

        dwgFileConverter.convertToPDF(inputFile, outputFile);

        verify(dwgToPdfService).convertDwgToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        DwgToPdfService dwgToPdfService = Mockito.mock(DwgToPdfService.class);
        DwgFileConverter dwgFileConverter = new DwgFileConverter(dwgToPdfService);
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.dwg", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new IOException("File not found")).when(dwgToPdfService).convertDwgToPdf(any(), any());

        assertThrows(RuntimeException.class, () -> dwgFileConverter.convertToPDF(inputFile, outputFile));
    }

    @Test
    void testConvertToPDF_NullPointerException_ThrowsNullPointerException() throws IOException {
        DwgToPdfService dwgToPdfService = Mockito.mock(DwgToPdfService.class);
        DwgFileConverter dwgFileConverter = new DwgFileConverter(dwgToPdfService);
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.dwg", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new NullPointerException("Null input")).when(dwgToPdfService).convertDwgToPdf(any(), any());

        assertThrows(NullPointerException.class, () -> dwgFileConverter.convertToPDF(inputFile, outputFile));
    }
}
