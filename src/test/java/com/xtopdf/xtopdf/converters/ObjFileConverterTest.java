package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.ObjToPdfService;
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

class ObjFileConverterTest {

    @Test
    void testConvertToPDF() throws IOException {
        ObjToPdfService objToPdfService = Mockito.mock(ObjToPdfService.class);
        ObjFileConverter objFileConverter = new ObjFileConverter(objToPdfService);
        var outputFile = "output.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.obj", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doNothing().when(objToPdfService).convertObjToPdf(any(), any());

        objFileConverter.convertToPDF(inputFile, outputFile, false);

        verify(objToPdfService).convertObjToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        ObjToPdfService objToPdfService = Mockito.mock(ObjToPdfService.class);
        ObjFileConverter objFileConverter = new ObjFileConverter(objToPdfService);
        var outputFile = "output.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.obj", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new IOException("File not found")).when(objToPdfService).convertObjToPdf(any(), any());

        assertThrows(RuntimeException.class, () -> objFileConverter.convertToPDF(inputFile, outputFile, false));
    }
}
