package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.HtmlToPdfService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

class HtmlFileConverterTest {

    @Test
    void testConvertToPDF() {
        HtmlToPdfService htmlToPdfService = Mockito.mock(HtmlToPdfService.class);
        HtmlFileConverter htmlFileConverter = new HtmlFileConverter(htmlToPdfService);
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.docx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doNothing().when(htmlToPdfService).convertHtmlToPdf(any(), any());

        htmlFileConverter.convertToPDF(inputFile, outputFile);

        verify(htmlToPdfService).convertHtmlToPdf(any(), any());
    }
}