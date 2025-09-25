package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.RtfToPdfService;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RtfFileConverterTest {

    @Mock
    private RtfToPdfService rtfToPdfService;

    private RtfFileConverter rtfFileConverter;

    @BeforeEach
    void setUp() {
        rtfFileConverter = new RtfFileConverter(rtfToPdfService);
    }

    @Test
    void convertToPDF_ValidInput_CallsService() throws IOException {
        MockMultipartFile rtfFile = new MockMultipartFile(
                "file", 
                "test.rtf", 
                "application/rtf", 
                "test content".getBytes()
        );
        String outputFile = "output.pdf";

        rtfFileConverter.convertToPDF(rtfFile, outputFile);

        verify(rtfToPdfService).convertRtfToPdf(eq(rtfFile), any(File.class));
    }

    @Test
    void convertToPDF_NullInputFile_ThrowsNullPointerException() {
        String outputFile = "output.pdf";

        assertThrows(NullPointerException.class, 
                () -> rtfFileConverter.convertToPDF(null, outputFile));
    }

    @Test
    void convertToPDF_NullOutputFile_ThrowsNullPointerException() {
        MockMultipartFile rtfFile = new MockMultipartFile(
                "file", 
                "test.rtf", 
                "application/rtf", 
                "test content".getBytes()
        );

        assertThrows(NullPointerException.class, 
                () -> rtfFileConverter.convertToPDF(rtfFile, null));
    }

    @Test
    void convertToPDF_ServiceThrowsIOException_WrapsInRuntimeException() throws IOException {
        MockMultipartFile rtfFile = new MockMultipartFile(
                "file", 
                "test.rtf", 
                "application/rtf", 
                "test content".getBytes()
        );
        String outputFile = "output.pdf";

        doThrow(new IOException("Test IOException")).when(rtfToPdfService)
                .convertRtfToPdf(any(), any());

        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> rtfFileConverter.convertToPDF(rtfFile, outputFile));
        
        assertTrue(exception.getMessage().contains("Error converting RTF to PDF"));
        assertTrue(exception.getCause() instanceof IOException);
    }

    @Test
    void convertToPDF_ValidInputWithSpecialCharacters_CallsService() throws IOException {
        MockMultipartFile rtfFile = new MockMultipartFile(
                "file", 
                "special.rtf", 
                "application/rtf", 
                "{\\rtf1 Special chars: àáâãäå}".getBytes()
        );
        String outputFile = "/path/to/output.pdf";

        rtfFileConverter.convertToPDF(rtfFile, outputFile);

        verify(rtfToPdfService).convertRtfToPdf(eq(rtfFile), any(File.class));
    }
}