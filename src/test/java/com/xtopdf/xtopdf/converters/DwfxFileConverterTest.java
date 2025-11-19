package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.DwfxToPdfService;
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

class DwfxFileConverterTest {

    @Test
    void testConvertToPDF() throws IOException {
        DwfxToPdfService dwfxToPdfService = Mockito.mock(DwfxToPdfService.class);
        DwfxFileConverter dwfxFileConverter = new DwfxFileConverter(dwfxToPdfService);
        var outputFile = "output.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.dwfx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doNothing().when(dwfxToPdfService).convertDwfxToPdf(any(), any());

        dwfxFileConverter.convertToPDF(inputFile, outputFile, false);

        verify(dwfxToPdfService).convertDwfxToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        DwfxToPdfService dwfxToPdfService = Mockito.mock(DwfxToPdfService.class);
        DwfxFileConverter dwfxFileConverter = new DwfxFileConverter(dwfxToPdfService);
        var outputFile = "output.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.dwfx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new IOException("File not found")).when(dwfxToPdfService).convertDwfxToPdf(any(), any());

        assertThrows(RuntimeException.class, () -> dwfxFileConverter.convertToPDF(inputFile, outputFile, false));
    }
}
