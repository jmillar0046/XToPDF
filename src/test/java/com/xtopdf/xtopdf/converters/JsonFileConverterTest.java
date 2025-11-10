package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.JsonToPdfService;
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
import com.xtopdf.xtopdf.services.PageNumberService;

class JsonFileConverterTest {

    @Test
    void testConvertToPDF() throws IOException {
        JsonToPdfService jsonToPdfService = Mockito.mock(JsonToPdfService.class);
        JsonFileConverter jsonFileConverter = new JsonFileConverter(jsonToPdfService, Mockito.mock(PageNumberService.class));
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.json", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doNothing().when(jsonToPdfService).convertJsonToPdf(any(), any());

        jsonFileConverter.convertToPDF(inputFile, outputFile);

        verify(jsonToPdfService).convertJsonToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        JsonToPdfService jsonToPdfService = Mockito.mock(JsonToPdfService.class);
        JsonFileConverter jsonFileConverter = new JsonFileConverter(jsonToPdfService, Mockito.mock(PageNumberService.class));
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.json", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new IOException("File not found")).when(jsonToPdfService).convertJsonToPdf(any(), any());

        assertThrows(RuntimeException.class, () -> jsonFileConverter.convertToPDF(inputFile, outputFile));
    }

    @Test
    void testConvertToPDF_NullPointerException_ThrowsNullPointerException() throws IOException {
        JsonToPdfService jsonToPdfService = Mockito.mock(JsonToPdfService.class);
        JsonFileConverter jsonFileConverter = new JsonFileConverter(jsonToPdfService, Mockito.mock(PageNumberService.class));
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.json", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new NullPointerException("Null input")).when(jsonToPdfService).convertJsonToPdf(any(), any());

        assertThrows(NullPointerException.class, () -> jsonFileConverter.convertToPDF(inputFile, outputFile));
    }
}
