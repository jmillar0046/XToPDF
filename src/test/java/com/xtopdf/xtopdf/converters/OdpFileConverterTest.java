package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import com.xtopdf.xtopdf.services.conversion.presentation.OdpToPdfService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OdpFileConverterTest {
    @Test
    void testConvertToPDF() throws IOException, FileConversionException {
        OdpToPdfService service = Mockito.mock(OdpToPdfService.class);
        OdpFileConverter converter = new OdpFileConverter(service);
        var inputFile = new MockMultipartFile("file", "test.odp", "application/vnd.oasis.opendocument.presentation", "content".getBytes());
        doNothing().when(service).convertOdpToPdf(any(), any());
        converter.convertToPDF(inputFile, "output.pdf");
        verify(service).convertOdpToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException, FileConversionException {
        OdpToPdfService service = Mockito.mock(OdpToPdfService.class);
        OdpFileConverter converter = new OdpFileConverter(service);
        var inputFile = new MockMultipartFile("file", "test.odp", "application/vnd.oasis.opendocument.presentation", "content".getBytes());
        doThrow(new IOException("Error")).when(service).convertOdpToPdf(any(), any());
        assertThrows(RuntimeException.class, () -> converter.convertToPDF(inputFile, "output.pdf"));
    }
}
