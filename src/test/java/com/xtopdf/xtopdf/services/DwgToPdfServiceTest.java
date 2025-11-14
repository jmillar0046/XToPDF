package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

class DwgToPdfServiceTest {

    private DwgToDxfService dwgToDxfService;
    private DxfToPdfService dxfToPdfService;
    private DwgToPdfService dwgToPdfService;

    @BeforeEach
    void setUp() {
        dwgToDxfService = Mockito.mock(DwgToDxfService.class);
        dxfToPdfService = Mockito.mock(DxfToPdfService.class);
        dwgToPdfService = new DwgToPdfService(dwgToDxfService, dxfToPdfService);
    }

    @Test
    void testConvertDwgToPdf_ThrowsUnsupportedOperationException() throws IOException {
        var content = "Sample DWG content";
        var dwgFile = new MockMultipartFile("file", "test.dwg", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        File pdfFile = new File(System.getProperty("java.io.tmpdir") + "/testDwgOutput.pdf");

        doThrow(new UnsupportedOperationException("Direct DWG to DXF conversion is not supported"))
            .when(dwgToDxfService).convertDwgToDxf(any(), any());

        assertThrows(UnsupportedOperationException.class, () -> dwgToPdfService.convertDwgToPdf(dwgFile, pdfFile));
    }

    @Test
    void testConvertDwgToPdf_WithNullInput_ThrowsNullPointerException() throws IOException {
        File pdfFile = new File(System.getProperty("java.io.tmpdir") + "/testOutput.pdf");

        doThrow(new NullPointerException("Null input"))
            .when(dwgToDxfService).convertDwgToDxf(any(), any());

        assertThrows(NullPointerException.class, () -> dwgToPdfService.convertDwgToPdf(null, pdfFile));
    }

    @Test
    void testConvertDwgToPdf_WithIOException_ThrowsIOException() throws IOException {
        var content = "Sample DWG content";
        var dwgFile = new MockMultipartFile("file", "test.dwg", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        File pdfFile = new File(System.getProperty("java.io.tmpdir") + "/testDwgOutput.pdf");

        doThrow(new IOException("IO error during conversion"))
            .when(dwgToDxfService).convertDwgToDxf(any(), any());

        assertThrows(IOException.class, () -> dwgToPdfService.convertDwgToPdf(dwgFile, pdfFile));
    }
}
