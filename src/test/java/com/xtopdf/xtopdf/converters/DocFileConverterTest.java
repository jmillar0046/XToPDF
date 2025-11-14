package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.DocToPdfService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DocFileConverterTest {
    @Test
    void testConvertToPDF() throws IOException {
        DocToPdfService service = Mockito.mock(DocToPdfService.class);
        DocFileConverter converter = new DocFileConverter(service);
        var inputFile = new MockMultipartFile("file", "test.doc", "application/msword", "content".getBytes());
        doNothing().when(service).convertDocToPdf(any(), any());
        converter.convertToPDF(inputFile, "output.pdf");
        verify(service).convertDocToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        DocToPdfService service = Mockito.mock(DocToPdfService.class);
        DocFileConverter converter = new DocFileConverter(service);
        var inputFile = new MockMultipartFile("file", "test.doc", "application/msword", "content".getBytes());
        doThrow(new IOException("Error")).when(service).convertDocToPdf(any(), any());
        assertThrows(RuntimeException.class, () -> converter.convertToPDF(inputFile, "output.pdf"));
    }
}
