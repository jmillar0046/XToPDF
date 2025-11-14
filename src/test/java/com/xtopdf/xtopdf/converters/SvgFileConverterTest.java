package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.SvgToPdfService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SvgFileConverterTest {
    @Test
    void testConvertToPDF() throws IOException {
        SvgToPdfService service = Mockito.mock(SvgToPdfService.class);
        SvgFileConverter converter = new SvgFileConverter(service);
        var inputFile = new MockMultipartFile("file", "test.svg", "image/svg+xml", "content".getBytes());
        doNothing().when(service).convertSvgToPdf(any(), any());
        converter.convertToPDF(inputFile, "output.pdf");
        verify(service).convertSvgToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        SvgToPdfService service = Mockito.mock(SvgToPdfService.class);
        SvgFileConverter converter = new SvgFileConverter(service);
        var inputFile = new MockMultipartFile("file", "test.svg", "image/svg+xml", "content".getBytes());
        doThrow(new IOException("Error")).when(service).convertSvgToPdf(any(), any());
        assertThrows(RuntimeException.class, () -> converter.convertToPDF(inputFile, "output.pdf"));
    }
}
