package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.TxtToPdfService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
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
}