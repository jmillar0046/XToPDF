package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.spreadsheet.TsvToPdfService;
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

class TsvFileConverterTest {

    @Test
    void testConvertToPDF() throws IOException {
        TsvToPdfService tsvToPdfService = Mockito.mock(TsvToPdfService.class);
        TsvFileConverter tsvFileConverter = new TsvFileConverter(tsvToPdfService);
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.tsv", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doNothing().when(tsvToPdfService).convertTsvToPdf(any(), any());

        tsvFileConverter.convertToPDF(inputFile, outputFile);

        verify(tsvToPdfService).convertTsvToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        TsvToPdfService tsvToPdfService = Mockito.mock(TsvToPdfService.class);
        TsvFileConverter tsvFileConverter = new TsvFileConverter(tsvToPdfService);
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.tsv", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new IOException("File not found")).when(tsvToPdfService).convertTsvToPdf(any(), any());

        assertThrows(RuntimeException.class, () -> tsvFileConverter.convertToPDF(inputFile, outputFile));
    }

    @Test
    void testConvertToPDF_NullPointerException_ThrowsNullPointerException() throws IOException {
        TsvToPdfService tsvToPdfService = Mockito.mock(TsvToPdfService.class);
        TsvFileConverter tsvFileConverter = new TsvFileConverter(tsvToPdfService);
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.tsv", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new NullPointerException("Null input")).when(tsvToPdfService).convertTsvToPdf(any(), any());

        assertThrows(NullPointerException.class, () -> tsvFileConverter.convertToPDF(inputFile, outputFile));
    }
}
