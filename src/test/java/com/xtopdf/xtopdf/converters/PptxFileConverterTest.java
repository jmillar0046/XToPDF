package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.PptxToPdfService;
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

class PptxFileConverterTest {

    @Test
    void testConvertToPDF() throws IOException {
        PptxToPdfService pptxToPdfService = Mockito.mock(PptxToPdfService.class);
        PptxFileConverter pptxFileConverter = new PptxFileConverter(pptxToPdfService);
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.pptx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doNothing().when(pptxToPdfService).convertPptxToPdf(any(), any());

        pptxFileConverter.convertToPDF(inputFile, outputFile);

        verify(pptxToPdfService).convertPptxToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        PptxToPdfService pptxToPdfService = Mockito.mock(PptxToPdfService.class);
        PptxFileConverter pptxFileConverter = new PptxFileConverter(pptxToPdfService);
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.pptx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new IOException("File processing error")).when(pptxToPdfService).convertPptxToPdf(any(), any());

        assertThrows(RuntimeException.class, () -> pptxFileConverter.convertToPDF(inputFile, outputFile));
    }

    @Test
    void testConvertToPDF_NullInputFile_ThrowsNullPointerException() {
        PptxToPdfService pptxToPdfService = Mockito.mock(PptxToPdfService.class);
        PptxFileConverter pptxFileConverter = new PptxFileConverter(pptxToPdfService);
        var outputFile = "outputFile.pdf";

        assertThrows(NullPointerException.class, () -> pptxFileConverter.convertToPDF(null, outputFile));
    }

    @Test
    void testConvertToPDF_NullOutputFile_ThrowsNullPointerException() {
        PptxToPdfService pptxToPdfService = Mockito.mock(PptxToPdfService.class);
        PptxFileConverter pptxFileConverter = new PptxFileConverter(pptxToPdfService);
        var inputFile = new MockMultipartFile("inputFile", "test.pptx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        assertThrows(NullPointerException.class, () -> pptxFileConverter.convertToPDF(inputFile, null));
    }
}