package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.TxtToPdfService;
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

class TxtFileConverterTest {

    @Test
    void testConvertToPDF() throws IOException {
        TxtToPdfService txtToPdfService = Mockito.mock(TxtToPdfService.class);
        TxtFileConverter txtFileConverter = new TxtFileConverter(txtToPdfService);
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.txt", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doNothing().when(txtToPdfService).convertTxtToPdf(any(), any());

        txtFileConverter.convertToPDF(inputFile, outputFile);

        verify(txtToPdfService).convertTxtToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        TxtToPdfService txtToPdfService = Mockito.mock(TxtToPdfService.class);
        TxtFileConverter txtFileConverter = new TxtFileConverter(txtToPdfService);
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.txt", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new IOException("File not found")).when(txtToPdfService).convertTxtToPdf(any(), any());

        assertThrows(RuntimeException.class, () -> txtFileConverter.convertToPDF(inputFile, outputFile));
    }

    @Test
    void testConvertToPDF_NullPointerException_ThrowsNullPointerException() throws IOException {
        TxtToPdfService txtToPdfService = Mockito.mock(TxtToPdfService.class);
        TxtFileConverter txtFileConverter = new TxtFileConverter(txtToPdfService);
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.txt", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new NullPointerException("Null input")).when(txtToPdfService).convertTxtToPdf(any(), any());

        assertThrows(NullPointerException.class, () -> txtFileConverter.convertToPDF(inputFile, outputFile));
    }
}