package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.PptToPdfService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PptFileConverterTest {
    @Test
    void testConvertToPDF() throws IOException {
        PptToPdfService service = Mockito.mock(PptToPdfService.class);
        PptFileConverter converter = new PptFileConverter(service);
        var inputFile = new MockMultipartFile("file", "test.ppt", "application/vnd.ms-powerpoint", "content".getBytes());
        doNothing().when(service).convertPptToPdf(any(), any());
        converter.convertToPDF(inputFile, "output.pdf");
        verify(service).convertPptToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        PptToPdfService service = Mockito.mock(PptToPdfService.class);
        PptFileConverter converter = new PptFileConverter(service);
        var inputFile = new MockMultipartFile("file", "test.ppt", "application/vnd.ms-powerpoint", "content".getBytes());
        doThrow(new IOException("Error")).when(service).convertPptToPdf(any(), any());
        assertThrows(RuntimeException.class, () -> converter.convertToPDF(inputFile, "output.pdf"));
    }
}
