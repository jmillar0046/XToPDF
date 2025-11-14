package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.DxfToPdfService;
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

class DxfFileConverterTest {

    @Test
    void testConvertToPDF() throws IOException {
        DxfToPdfService dxfToPdfService = Mockito.mock(DxfToPdfService.class);
        DxfFileConverter dxfFileConverter = new DxfFileConverter(dxfToPdfService);
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doNothing().when(dxfToPdfService).convertDxfToPdf(any(), any());

        dxfFileConverter.convertToPDF(inputFile, outputFile);

        verify(dxfToPdfService).convertDxfToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        DxfToPdfService dxfToPdfService = Mockito.mock(DxfToPdfService.class);
        DxfFileConverter dxfFileConverter = new DxfFileConverter(dxfToPdfService);
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new IOException("File not found")).when(dxfToPdfService).convertDxfToPdf(any(), any());

        assertThrows(RuntimeException.class, () -> dxfFileConverter.convertToPDF(inputFile, outputFile));
    }

    @Test
    void testConvertToPDF_NullPointerException_ThrowsNullPointerException() throws IOException {
        DxfToPdfService dxfToPdfService = Mockito.mock(DxfToPdfService.class);
        DxfFileConverter dxfFileConverter = new DxfFileConverter(dxfToPdfService);
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new NullPointerException("Null input")).when(dxfToPdfService).convertDxfToPdf(any(), any());

        assertThrows(NullPointerException.class, () -> dxfFileConverter.convertToPDF(inputFile, outputFile));
    }
}
