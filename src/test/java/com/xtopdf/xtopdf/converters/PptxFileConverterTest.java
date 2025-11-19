package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.PptxToPdfService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PptxFileConverterTest {
    @Test
    void testConvertToPDF() throws IOException {
        PptxToPdfService service = Mockito.mock(PptxToPdfService.class);
        PptxFileConverter converter = new PptxFileConverter(service);
        var inputFile = new MockMultipartFile("file", "test.pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation", "content".getBytes());
        doNothing().when(service).convertPptxToPdf(any(), any());
        converter.convertToPDF(inputFile, "output.pdf");
        verify(service).convertPptxToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        PptxToPdfService service = Mockito.mock(PptxToPdfService.class);
        PptxFileConverter converter = new PptxFileConverter(service);
        var inputFile = new MockMultipartFile("file", "test.pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation", "content".getBytes());
        doThrow(new IOException("Error")).when(service).convertPptxToPdf(any(), any());
        assertThrows(RuntimeException.class, () -> converter.convertToPDF(inputFile, "output.pdf"));
    }
}
