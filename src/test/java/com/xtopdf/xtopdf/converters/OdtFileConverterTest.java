package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.OdtToPdfService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OdtFileConverterTest {
    @Test
    void testConvertToPDF() throws IOException {
        OdtToPdfService service = Mockito.mock(OdtToPdfService.class);
        OdtFileConverter converter = new OdtFileConverter(service);
        var inputFile = new MockMultipartFile("file", "test.odt", "application/vnd.oasis.opendocument.text", "content".getBytes());
        doNothing().when(service).convertOdtToPdf(any(), any());
        converter.convertToPDF(inputFile, "output.pdf");
        verify(service).convertOdtToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        OdtToPdfService service = Mockito.mock(OdtToPdfService.class);
        OdtFileConverter converter = new OdtFileConverter(service);
        var inputFile = new MockMultipartFile("file", "test.odt", "application/vnd.oasis.opendocument.text", "content".getBytes());
        doThrow(new IOException("Error")).when(service).convertOdtToPdf(any(), any());
        assertThrows(RuntimeException.class, () -> converter.convertToPDF(inputFile, "output.pdf"));
    }
}
