package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.JpegToPdfService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import com.xtopdf.xtopdf.services.PageNumberService;

@ExtendWith(MockitoExtension.class)
class JpegFileConverterTest {

    @Mock
    private JpegToPdfService jpegToPdfService;
    @Mock
    private PageNumberService pageNumberService;

    private JpegFileConverter jpegFileConverter;

    @BeforeEach
    void setUp() {
        jpegFileConverter = new JpegFileConverter(jpegToPdfService, pageNumberService);
    }

    @Test
    void convertToPDF_ValidJpegFile_CallsService() throws IOException {
        MockMultipartFile jpegFile = new MockMultipartFile("test.jpeg", "test.jpeg", "image/jpeg", "fake jpeg content".getBytes());
        String outputFile = "output.pdf";

        jpegFileConverter.convertToPDF(jpegFile, outputFile);

        verify(jpegToPdfService).convertJpegToPdf(eq(jpegFile), any(File.class));
    }

    @Test
    void convertToPDF_ServiceThrowsIOException_ThrowsRuntimeException() throws IOException {
        MockMultipartFile jpegFile = new MockMultipartFile("test.jpeg", "test.jpeg", "image/jpeg", "fake jpeg content".getBytes());
        String outputFile = "output.pdf";

        doThrow(new IOException("Service error")).when(jpegToPdfService)
                .convertJpegToPdf(eq(jpegFile), any(File.class));

        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> jpegFileConverter.convertToPDF(jpegFile, outputFile));
        
        assertTrue(exception.getMessage().contains("Error converting JPEG to PDF"));
        assertTrue(exception.getCause() instanceof IOException);
    }

    @Test
    void convertToPDF_NullInputFile_ThrowsNullPointerException() {
        String outputFile = "output.pdf";

        assertThrows(NullPointerException.class, 
                () -> jpegFileConverter.convertToPDF(null, outputFile));
    }

    @Test
    void convertToPDF_NullOutputFile_ThrowsNullPointerException() {
        MockMultipartFile jpegFile = new MockMultipartFile("test.jpeg", "test.jpeg", "image/jpeg", "fake jpeg content".getBytes());

        assertThrows(NullPointerException.class, 
                () -> jpegFileConverter.convertToPDF(jpegFile, null));
    }
}