package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.services.conversion.spreadsheet.ExcelToPdfService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

class XlsFileConverterTest {

    @Test
    void testConvertToPDF() throws IOException, FileConversionException {
        ExcelToPdfService service = Mockito.mock(ExcelToPdfService.class);
        XlsFileConverter converter = new XlsFileConverter(service);
        var inputFile = new MockMultipartFile("file", "test.xls", "application/vnd.ms-excel", "content".getBytes());

        doNothing().when(service).convertExcelToPdf(any(), any(), anyBoolean());

        converter.convertToPDF(inputFile, "output.pdf");

        verify(service).convertExcelToPdf(any(), any(), anyBoolean());
    }

    @Test
    void testConvertToPDF_IOException_ThrowsFileConversionException() throws IOException {
        ExcelToPdfService service = Mockito.mock(ExcelToPdfService.class);
        XlsFileConverter converter = new XlsFileConverter(service);
        var inputFile = new MockMultipartFile("file", "test.xls", "application/vnd.ms-excel", "content".getBytes());

        doThrow(new IOException("Error")).when(service).convertExcelToPdf(any(), any(), anyBoolean());

        FileConversionException ex = assertThrows(FileConversionException.class,
                () -> converter.convertToPDF(inputFile, "output.pdf"));
        assertTrue(ex.getMessage().contains("XLS"), "Exception message should contain 'XLS'");
    }

    @Test
    void testConvertToPDF_IOException_DoesNotThrowRuntimeException() throws IOException {
        ExcelToPdfService service = Mockito.mock(ExcelToPdfService.class);
        XlsFileConverter converter = new XlsFileConverter(service);
        var inputFile = new MockMultipartFile("file", "test.xls", "application/vnd.ms-excel", "content".getBytes());

        doThrow(new IOException("Error")).when(service).convertExcelToPdf(any(), any(), anyBoolean());

        // Should throw FileConversionException, not RuntimeException
        Exception thrown = null;
        try {
            converter.convertToPDF(inputFile, "output.pdf");
            fail("Expected an exception to be thrown");
        } catch (Exception e) {
            thrown = e;
        }
        assertNotNull(thrown);
        assertInstanceOf(FileConversionException.class, thrown);
        assertFalse(thrown instanceof RuntimeException, "Should not throw RuntimeException");
    }

    @Test
    void testConvertToPDF_DoesNotCatchNullPointerException() throws IOException {
        ExcelToPdfService service = Mockito.mock(ExcelToPdfService.class);
        XlsFileConverter converter = new XlsFileConverter(service);
        var inputFile = new MockMultipartFile("file", "test.xls", "application/vnd.ms-excel", "content".getBytes());

        doThrow(new NullPointerException("null value")).when(service).convertExcelToPdf(any(), any(), anyBoolean());

        // NullPointerException should propagate as-is, not be caught and re-wrapped
        assertThrows(NullPointerException.class, () -> converter.convertToPDF(inputFile, "output.pdf"));
    }
}
