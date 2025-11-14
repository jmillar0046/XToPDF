package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.OdsToPdfService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OdsFileConverterTest {
    @Test
    void testConvertToPDF() throws IOException {
        OdsToPdfService service = Mockito.mock(OdsToPdfService.class);
        OdsFileConverter converter = new OdsFileConverter(service);
        var inputFile = new MockMultipartFile("file", "test.ods", "application/vnd.oasis.opendocument.spreadsheet", "content".getBytes());
        doNothing().when(service).convertOdsToPdf(any(), any());
        converter.convertToPDF(inputFile, "output.pdf");
        verify(service).convertOdsToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        OdsToPdfService service = Mockito.mock(OdsToPdfService.class);
        OdsFileConverter converter = new OdsFileConverter(service);
        var inputFile = new MockMultipartFile("file", "test.ods", "application/vnd.oasis.opendocument.spreadsheet", "content".getBytes());
        doThrow(new IOException("Error")).when(service).convertOdsToPdf(any(), any());
        assertThrows(RuntimeException.class, () -> converter.convertToPDF(inputFile, "output.pdf"));
    }
}
