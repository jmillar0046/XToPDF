package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.DocxToPdfService;
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


class DocxFileConverterTest {

    @Test
    void testConvertToPDF() throws IOException {
        DocxToPdfService docxToPdfService = Mockito.mock(DocxToPdfService.class);
        DocxFileConverter docxFileConverter = new DocxFileConverter(docxToPdfService);
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.docx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doNothing().when(docxToPdfService).convertDocxToPdf(any(), any());

        docxFileConverter.convertToPDF(inputFile, outputFile);

        verify(docxToPdfService).convertDocxToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        DocxToPdfService docxToPdfService = Mockito.mock(DocxToPdfService.class);
        DocxFileConverter docxFileConverter = new DocxFileConverter(docxToPdfService);
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.docx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new IOException("File processing error")).when(docxToPdfService).convertDocxToPdf(any(), any());

        assertThrows(RuntimeException.class, () -> docxFileConverter.convertToPDF(inputFile, outputFile));
    }
}