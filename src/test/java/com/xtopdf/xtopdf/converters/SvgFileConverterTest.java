package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.SvgToPdfService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SvgFileConverterTest {

    @Mock
    private SvgToPdfService svgToPdfService;

    @InjectMocks
    private SvgFileConverter svgFileConverter;

    @Test
    void testConvertToPDFSuccessfully() throws IOException {
        String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100\" height=\"100\"></svg>";
        MockMultipartFile svgFile = new MockMultipartFile("file", "test.svg", "image/svg+xml", svg.getBytes());
        String outputFile = "output.pdf";

        svgFileConverter.convertToPDF(svgFile, outputFile);

        verify(svgToPdfService).convertSvgToPdf(eq(svgFile), any(File.class));
    }

    @Test
    void testConvertToPDFWithNullInputFileThrowsException() {
        String outputFile = "output.pdf";

        assertThrows(NullPointerException.class, () -> 
            svgFileConverter.convertToPDF(null, outputFile));

        verifyNoInteractions(svgToPdfService);
    }

    @Test
    void testConvertToPDFWithNullOutputFileThrowsException() {
        String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100\" height=\"100\"></svg>";
        MockMultipartFile svgFile = new MockMultipartFile("file", "test.svg", "image/svg+xml", svg.getBytes());

        assertThrows(NullPointerException.class, () -> 
            svgFileConverter.convertToPDF(svgFile, null));

        verifyNoInteractions(svgToPdfService);
    }

    @Test
    void testConvertToPDFWithIOExceptionThrowsRuntimeException() throws IOException {
        String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100\" height=\"100\"></svg>";
        MockMultipartFile svgFile = new MockMultipartFile("file", "test.svg", "image/svg+xml", svg.getBytes());
        String outputFile = "output.pdf";

        doThrow(new IOException("Test exception")).when(svgToPdfService)
            .convertSvgToPdf(eq(svgFile), any(File.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            svgFileConverter.convertToPDF(svgFile, outputFile));

        assertTrue(exception.getMessage().contains("Error converting SVG to PDF"));
        assertTrue(exception.getCause() instanceof IOException);
        verify(svgToPdfService).convertSvgToPdf(eq(svgFile), any(File.class));
    }
}