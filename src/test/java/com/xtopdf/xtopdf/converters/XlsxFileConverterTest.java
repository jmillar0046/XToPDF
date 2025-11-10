package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.XlsxToPdfService;
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

class XlsxFileConverterTest {

    @Test
    void testConvertToPDF() throws IOException {
        XlsxToPdfService xlsxToPdfService = Mockito.mock(XlsxToPdfService.class);
        XlsxFileConverter xlsxFileConverter = new XlsxFileConverter(xlsxToPdfService);
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doNothing().when(xlsxToPdfService).convertXlsxToPdf(any(), any());

        xlsxFileConverter.convertToPDF(inputFile, outputFile);

        verify(xlsxToPdfService).convertXlsxToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        XlsxToPdfService xlsxToPdfService = Mockito.mock(XlsxToPdfService.class);
        XlsxFileConverter xlsxFileConverter = new XlsxFileConverter(xlsxToPdfService);
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new IOException("File processing error")).when(xlsxToPdfService).convertXlsxToPdf(any(), any());

        assertThrows(RuntimeException.class, () -> xlsxFileConverter.convertToPDF(inputFile, outputFile));
    }
}