package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.CsvToPdfService;
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

class CsvFileConverterTest {

    @Test
    void testConvertToPDF() throws IOException {
        CsvToPdfService csvToPdfService = Mockito.mock(CsvToPdfService.class);
        CsvFileConverter csvFileConverter = new CsvFileConverter(csvToPdfService);
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.csv", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doNothing().when(csvToPdfService).convertCsvToPdf(any(), any());

        csvFileConverter.convertToPDF(inputFile, outputFile);

        verify(csvToPdfService).convertCsvToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        CsvToPdfService csvToPdfService = Mockito.mock(CsvToPdfService.class);
        CsvFileConverter csvFileConverter = new CsvFileConverter(csvToPdfService);
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.csv", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new IOException("File not found")).when(csvToPdfService).convertCsvToPdf(any(), any());

        assertThrows(RuntimeException.class, () -> csvFileConverter.convertToPDF(inputFile, outputFile));
    }

    @Test
    void testConvertToPDF_NullPointerException_ThrowsNullPointerException() throws IOException {
        CsvToPdfService csvToPdfService = Mockito.mock(CsvToPdfService.class);
        CsvFileConverter csvFileConverter = new CsvFileConverter(csvToPdfService);
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.csv", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new NullPointerException("Null input")).when(csvToPdfService).convertCsvToPdf(any(), any());

        assertThrows(NullPointerException.class, () -> csvFileConverter.convertToPDF(inputFile, outputFile));
    }
}
