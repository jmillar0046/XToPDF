package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.XmlToPdfService;
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

class XmlFileConverterTest {

    @Test
    void testConvertToPDF() throws IOException {
        XmlToPdfService xmlToPdfService = Mockito.mock(XmlToPdfService.class);
        XmlFileConverter xmlFileConverter = new XmlFileConverter(xmlToPdfService);
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.xml", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doNothing().when(xmlToPdfService).convertXmlToPdf(any(), any());

        xmlFileConverter.convertToPDF(inputFile, outputFile);

        verify(xmlToPdfService).convertXmlToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        XmlToPdfService xmlToPdfService = Mockito.mock(XmlToPdfService.class);
        XmlFileConverter xmlFileConverter = new XmlFileConverter(xmlToPdfService);
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.xml", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new IOException("File not found")).when(xmlToPdfService).convertXmlToPdf(any(), any());

        assertThrows(RuntimeException.class, () -> xmlFileConverter.convertToPDF(inputFile, outputFile));
    }

    @Test
    void testConvertToPDF_NullPointerException_ThrowsNullPointerException() throws IOException {
        XmlToPdfService xmlToPdfService = Mockito.mock(XmlToPdfService.class);
        XmlFileConverter xmlFileConverter = new XmlFileConverter(xmlToPdfService);
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.xml", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new NullPointerException("Null input")).when(xmlToPdfService).convertXmlToPdf(any(), any());

        assertThrows(NullPointerException.class, () -> xmlFileConverter.convertToPDF(inputFile, outputFile));
    }
}
