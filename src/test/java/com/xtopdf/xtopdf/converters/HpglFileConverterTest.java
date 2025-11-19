package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.HpglToPdfService;
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

class HpglFileConverterTest {

    @Test
    void testConvertToPDF() throws IOException {
        HpglToPdfService hpglToPdfService = Mockito.mock(HpglToPdfService.class);
        HpglFileConverter hpglFileConverter = new HpglFileConverter(hpglToPdfService);
        var outputFile = "output.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.hpgl", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doNothing().when(hpglToPdfService).convertHpglToPdf(any(), any());

        hpglFileConverter.convertToPDF(inputFile, outputFile, false);

        verify(hpglToPdfService).convertHpglToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        HpglToPdfService hpglToPdfService = Mockito.mock(HpglToPdfService.class);
        HpglFileConverter hpglFileConverter = new HpglFileConverter(hpglToPdfService);
        var outputFile = "output.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.hpgl", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new IOException("File not found")).when(hpglToPdfService).convertHpglToPdf(any(), any());

        assertThrows(RuntimeException.class, () -> hpglFileConverter.convertToPDF(inputFile, outputFile, false));
    }
}
