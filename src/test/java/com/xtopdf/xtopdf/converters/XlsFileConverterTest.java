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
        assertTrue(ex.getMessage().contains("Error"), "Exception message should contain original error");
    }

    @Test
    void testConvertToPDF_NullPointerException_WrappedInFileConversionException() throws IOException {
        ExcelToPdfService service = Mockito.mock(ExcelToPdfService.class);
        XlsFileConverter converter = new XlsFileConverter(service);
        var inputFile = new MockMultipartFile("file", "test.xls", "application/vnd.ms-excel", "content".getBytes());

        doThrow(new NullPointerException("null value")).when(service).convertExcelToPdf(any(), any(), anyBoolean());

        // All exceptions are wrapped in FileConversionException per standardized handling
        assertThrows(FileConversionException.class, () -> converter.convertToPDF(inputFile, "output.pdf"));
    }
}
