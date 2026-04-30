package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.services.conversion.spreadsheet.ExcelToPdfService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

class XlsxFileConverterTest {

    @Test
    void testConvertToPDF() throws IOException, FileConversionException {
        ExcelToPdfService service = Mockito.mock(ExcelToPdfService.class);
        XlsxFileConverter converter = new XlsxFileConverter(service);
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doNothing().when(service).convertExcelToPdf(any(), any(), any(Boolean.class));

        converter.convertToPDF(inputFile, outputFile);

        verify(service).convertExcelToPdf(any(), any(), any(Boolean.class));
    }

    @Test
    void testConvertToPDF_IOException_ThrowsFileConversionException() throws IOException {
        ExcelToPdfService service = Mockito.mock(ExcelToPdfService.class);
        XlsxFileConverter converter = new XlsxFileConverter(service);
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new IOException("File processing error")).when(service).convertExcelToPdf(any(), any(), any(Boolean.class));

        FileConversionException ex = assertThrows(FileConversionException.class,
                () -> converter.convertToPDF(inputFile, outputFile));
        assertTrue(ex.getMessage().contains("XLSX"), "Exception message should contain 'XLSX'");
    }

    @Test
    void testConvertToPDF_IOException_DoesNotThrowRuntimeException() throws IOException {
        ExcelToPdfService service = Mockito.mock(ExcelToPdfService.class);
        XlsxFileConverter converter = new XlsxFileConverter(service);
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new IOException("File processing error")).when(service).convertExcelToPdf(any(), any(), any(Boolean.class));

        // Should throw FileConversionException, not RuntimeException
        Exception thrown = null;
        try {
            converter.convertToPDF(inputFile, outputFile);
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
        XlsxFileConverter converter = new XlsxFileConverter(service);
        var outputFile = "outputFile.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        doThrow(new NullPointerException("null value")).when(service).convertExcelToPdf(any(), any(), any(Boolean.class));

        // NullPointerException should propagate as-is, not be caught and re-wrapped
        assertThrows(NullPointerException.class, () -> converter.convertToPDF(inputFile, outputFile));
    }
}
