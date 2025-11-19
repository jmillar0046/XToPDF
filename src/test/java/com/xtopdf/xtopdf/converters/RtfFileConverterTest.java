package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.RtfToPdfService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RtfFileConverterTest {
    @Test
    void testConvertToPDF() throws IOException {
        RtfToPdfService service = Mockito.mock(RtfToPdfService.class);
        RtfFileConverter converter = new RtfFileConverter(service);
        var inputFile = new MockMultipartFile("file", "test.rtf", "application/rtf", "content".getBytes());
        doNothing().when(service).convertRtfToPdf(any(), any());
        converter.convertToPDF(inputFile, "output.pdf");
        verify(service).convertRtfToPdf(any(), any());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsRuntimeException() throws IOException {
        RtfToPdfService service = Mockito.mock(RtfToPdfService.class);
        RtfFileConverter converter = new RtfFileConverter(service);
        var inputFile = new MockMultipartFile("file", "test.rtf", "application/rtf", "content".getBytes());
        doThrow(new IOException("Error")).when(service).convertRtfToPdf(any(), any());
        assertThrows(RuntimeException.class, () -> converter.convertToPDF(inputFile, "output.pdf"));
    }
}
