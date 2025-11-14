package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.BmpToPdfService;
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

class BmpFileConverterTest {

    @Test
    void testConvertToPDF() throws IOException {
        BmpToPdfService bmpToPdfService = Mockito.mock(BmpToPdfService.class);
        BmpFileConverter bmpFileConverter = new BmpFileConverter(bmpToPdfService);
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.bmp", MediaType.IMAGE_PNG_VALUE, "test content".getBytes());

        doNothing().when(bmpToPdfService).convertBmpToPdf(any(), any());

        bmpFileConverter.convertToPDF(inputFile, outputFile);

        verify(bmpToPdfService).convertBmpToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        BmpToPdfService bmpToPdfService = Mockito.mock(BmpToPdfService.class);
        BmpFileConverter bmpFileConverter = new BmpFileConverter(bmpToPdfService);
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.bmp", MediaType.IMAGE_PNG_VALUE, "test content".getBytes());

        doThrow(new IOException("File not found")).when(bmpToPdfService).convertBmpToPdf(any(), any());

        assertThrows(RuntimeException.class, () -> bmpFileConverter.convertToPDF(inputFile, outputFile));
    }

    @Test
    void testConvertToPDF_NullInput_ThrowsNullPointerException() {
        BmpToPdfService bmpToPdfService = Mockito.mock(BmpToPdfService.class);
        BmpFileConverter bmpFileConverter = new BmpFileConverter(bmpToPdfService);

        assertThrows(NullPointerException.class, () -> bmpFileConverter.convertToPDF(null, "output.pdf"));
    }

    @Test
    void testConvertToPDF_NullOutput_ThrowsNullPointerException() {
        BmpToPdfService bmpToPdfService = Mockito.mock(BmpToPdfService.class);
        BmpFileConverter bmpFileConverter = new BmpFileConverter(bmpToPdfService);
        var inputFile = new MockMultipartFile("inputFile", "test.bmp", MediaType.IMAGE_PNG_VALUE, "test content".getBytes());

        assertThrows(NullPointerException.class, () -> bmpFileConverter.convertToPDF(inputFile, null));
    }
}
