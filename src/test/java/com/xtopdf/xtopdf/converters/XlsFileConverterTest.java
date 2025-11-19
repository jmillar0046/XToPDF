package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.XlsToPdfService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class XlsFileConverterTest {
    @Test
    void testConvertToPDF() throws IOException {
        XlsToPdfService service = Mockito.mock(XlsToPdfService.class);
        XlsFileConverter converter = new XlsFileConverter(service);
        var inputFile = new MockMultipartFile("file", "test.xls", "application/vnd.ms-excel", "content".getBytes());
        doNothing().when(service).convertXlsToPdf(any(), any(), anyBoolean());
        converter.convertToPDF(inputFile, "output.pdf");
        verify(service).convertXlsToPdf(any(), any(), anyBoolean());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        XlsToPdfService service = Mockito.mock(XlsToPdfService.class);
        XlsFileConverter converter = new XlsFileConverter(service);
        var inputFile = new MockMultipartFile("file", "test.xls", "application/vnd.ms-excel", "content".getBytes());
        doThrow(new IOException("Error")).when(service).convertXlsToPdf(any(), any(), anyBoolean());
        assertThrows(RuntimeException.class, () -> converter.convertToPDF(inputFile, "output.pdf"));
    }
}
