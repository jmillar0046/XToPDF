package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.HtmlToPdfService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import com.xtopdf.xtopdf.services.PageNumberService;

class HtmlFileConverterTest {

    @Test
    void testConvertToPDF() {
        HtmlToPdfService htmlToPdfService = Mockito.mock(HtmlToPdfService.class);
        HtmlFileConverter htmlFileConverter = new HtmlFileConverter(htmlToPdfService, Mockito.mock(PageNumberService.class));
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.html", MediaType.TEXT_HTML_VALUE, "test content".getBytes());

        doNothing().when(htmlToPdfService).convertHtmlToPdf(any(), any());

        htmlFileConverter.convertToPDF(inputFile, outputFile);

        verify(htmlToPdfService).convertHtmlToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_NullInputFile_ThrowsException() {
        HtmlToPdfService htmlToPdfService = Mockito.mock(HtmlToPdfService.class);
        HtmlFileConverter htmlFileConverter = new HtmlFileConverter(htmlToPdfService, Mockito.mock(PageNumberService.class));
        String outputFile = "outputFile.pdf";
        try {
            htmlFileConverter.convertToPDF(null, outputFile);
            assert false : "Expected NullPointerException for null input file";
        } catch (NullPointerException e) {
            assert true;
        }
    }

    @Test
    void testConvertToPDF_NullOutputFile_ThrowsException() {
        HtmlToPdfService htmlToPdfService = Mockito.mock(HtmlToPdfService.class);
        HtmlFileConverter htmlFileConverter = new HtmlFileConverter(htmlToPdfService, Mockito.mock(PageNumberService.class));
        var inputFile = new MockMultipartFile("inputFile", "test.html", MediaType.TEXT_HTML_VALUE, "test content".getBytes());
        try {
            htmlFileConverter.convertToPDF(inputFile, null);
            assert false : "Expected NullPointerException for null output file";
        } catch (NullPointerException e) {
            assert true;
        }
    }

    @Test
    void testConvertToPDF_InvalidFileType() {
        HtmlToPdfService htmlToPdfService = Mockito.mock(HtmlToPdfService.class);
        HtmlFileConverter htmlFileConverter = new HtmlFileConverter(htmlToPdfService, Mockito.mock(PageNumberService.class));
        var inputFile = new MockMultipartFile("inputFile", "test.txt", MediaType.TEXT_PLAIN_VALUE, "plain text".getBytes());
        var outputFile = "outputFile.pdf";
        doNothing().when(htmlToPdfService).convertHtmlToPdf(any(), any());
        htmlFileConverter.convertToPDF(inputFile, outputFile);
        verify(htmlToPdfService).convertHtmlToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_ServiceThrowsException() {
        HtmlToPdfService htmlToPdfService = Mockito.mock(HtmlToPdfService.class);
        HtmlFileConverter htmlFileConverter = new HtmlFileConverter(htmlToPdfService, Mockito.mock(PageNumberService.class));
        var inputFile = new MockMultipartFile("inputFile", "test.html", MediaType.TEXT_HTML_VALUE, "test".getBytes());
        var outputFile = "outputFile.pdf";
        Mockito.doThrow(new RuntimeException("Service error")).when(htmlToPdfService).convertHtmlToPdf(any(), any());
        try {
            htmlFileConverter.convertToPDF(inputFile, outputFile);
            assert false : "Expected RuntimeException from service";
        } catch (RuntimeException e) {
            assert e.getMessage().equals("Service error");
        }
    }

    @Test
    void testConvertToPDF_EmptyFile() {
        HtmlToPdfService htmlToPdfService = Mockito.mock(HtmlToPdfService.class);
        HtmlFileConverter htmlFileConverter = new HtmlFileConverter(htmlToPdfService, Mockito.mock(PageNumberService.class));
        var inputFile = new MockMultipartFile("inputFile", "empty.html", MediaType.TEXT_HTML_VALUE, new byte[0]);
        var outputFile = "outputFile.pdf";
        doNothing().when(htmlToPdfService).convertHtmlToPdf(any(), any());
        htmlFileConverter.convertToPDF(inputFile, outputFile);
        verify(htmlToPdfService).convertHtmlToPdf(any(), any());
    }
}
