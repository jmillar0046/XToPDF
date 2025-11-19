package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.StepToPdfService;
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

class StepFileConverterTest {

    @Test
    void testConvertToPDF() throws IOException {
        StepToPdfService stepToPdfService = Mockito.mock(StepToPdfService.class);
        StepFileConverter stepFileConverter = new StepFileConverter(stepToPdfService);
        var outputFile = "output.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.step", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doNothing().when(stepToPdfService).convertStepToPdf(any(), any());

        stepFileConverter.convertToPDF(inputFile, outputFile, false);

        verify(stepToPdfService).convertStepToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        StepToPdfService stepToPdfService = Mockito.mock(StepToPdfService.class);
        StepFileConverter stepFileConverter = new StepFileConverter(stepToPdfService);
        var outputFile = "output.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.step", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new IOException("File not found")).when(stepToPdfService).convertStepToPdf(any(), any());

        assertThrows(RuntimeException.class, () -> stepFileConverter.convertToPDF(inputFile, outputFile, false));
    }
}
