package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.services.conversion.document.DocxToPdfService;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;


class DocxFileConverterTest {

    private final DocxToPdfService docxToPdfService = Mockito.mock(DocxToPdfService.class);
    private final DocxFileConverter docxFileConverter = new DocxFileConverter(docxToPdfService);
    private final MockMultipartFile inputFile = new MockMultipartFile(
            "inputFile", "test.docx",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            "test content".getBytes());
    private final String outputFile = "outputFile.pdf";

    @Test
    void testConvertToPDF() throws IOException, FileConversionException {
        doNothing().when(docxToPdfService).convertDocxToPdf(any(), any());

        assertDoesNotThrow(() -> docxFileConverter.convertToPDF(inputFile, outputFile));

        verify(docxToPdfService).convertDocxToPdf(any(), any());
    }

    @Test
    void testIOExceptionIsWrappedInFileConversionException() throws IOException, FileConversionException {
        doThrow(new IOException("File processing error"))
                .when(docxToPdfService).convertDocxToPdf(any(), any());

        assertThatThrownBy(() -> docxFileConverter.convertToPDF(inputFile, outputFile))
                .isInstanceOf(FileConversionException.class);
    }

    @Test
    void testExceptionMessageContainsDOCX() throws IOException, FileConversionException {
        doThrow(new IOException("File processing error"))
                .when(docxToPdfService).convertDocxToPdf(any(), any());

        assertThatThrownBy(() -> docxFileConverter.convertToPDF(inputFile, outputFile))
                .isInstanceOf(FileConversionException.class)
                .hasMessageContaining("DOCX");
    }

    @Test
    void testOriginalIOExceptionIsPreservedAsCause() throws IOException, FileConversionException {
        IOException originalException = new IOException("File processing error");
        doThrow(originalException)
                .when(docxToPdfService).convertDocxToPdf(any(), any());

        assertThatThrownBy(() -> docxFileConverter.convertToPDF(inputFile, outputFile))
                .isInstanceOf(FileConversionException.class)
                .hasCause(originalException);
    }

    @Test
    void testRuntimeExceptionIsNotThrownForConversionFailures() throws IOException, FileConversionException {
        doThrow(new IOException("File processing error"))
                .when(docxToPdfService).convertDocxToPdf(any(), any());

        try {
            docxFileConverter.convertToPDF(inputFile, outputFile);
        } catch (RuntimeException e) {
            // RuntimeException should NOT be thrown for conversion failures
            assertThat(e).as("Should not throw RuntimeException for conversion failures")
                    .isNotInstanceOf(RuntimeException.class);
        } catch (FileConversionException e) {
            // This is the expected exception type — pass
            assertThat(e).isInstanceOf(FileConversionException.class);
        }
    }
}
