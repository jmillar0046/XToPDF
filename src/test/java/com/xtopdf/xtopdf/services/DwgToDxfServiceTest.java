package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DwgToDxfServiceTest {

    private DwgToDxfService dwgToDxfService;
    private File dxfFile;

    @BeforeEach
    void setUp() {
        dwgToDxfService = new DwgToDxfService();
    }

    @Test
    void testConvertDwgToDxf_ThrowsUnsupportedOperationException() {
        var content = "Sample DWG content";
        var dwgFile = new MockMultipartFile("file", "test.dwg", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        dxfFile = new File(System.getProperty("java.io.tmpdir") + "/testDwgToDxfOutput.dxf");

        UnsupportedOperationException exception = assertThrows(
            UnsupportedOperationException.class,
            () -> dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile),
            "Expected UnsupportedOperationException to be thrown"
        );

        assertTrue(exception.getMessage().contains("Direct DWG to DXF conversion is not supported"),
            "Exception message should indicate DWG to DXF conversion is not supported");
    }

    @Test
    void testConvertDwgToDxf_WithNullFile_ThrowsUnsupportedOperationException() {
        dxfFile = new File(System.getProperty("java.io.tmpdir") + "/testOutput.dxf");
        
        assertThrows(UnsupportedOperationException.class, () -> dwgToDxfService.convertDwgToDxf(null, dxfFile));
    }

    @Test
    void testConvertDwgToDxf_WithValidFile_ThrowsUnsupportedOperationException() {
        var content = "Valid DWG file content";
        var dwgFile = new MockMultipartFile("file", "valid.dwg", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        dxfFile = new File(System.getProperty("java.io.tmpdir") + "/validOutput.dxf");

        assertThrows(UnsupportedOperationException.class, () -> dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile));
    }
}
