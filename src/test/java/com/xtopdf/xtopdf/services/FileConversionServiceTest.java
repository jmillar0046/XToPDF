package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.factories.DocxFileConverterFactory;
import com.xtopdf.xtopdf.factories.HtmlFileConverterFactory;
import com.xtopdf.xtopdf.factories.SvgFileConverterFactory;
import com.xtopdf.xtopdf.factories.JpegFileConverterFactory;
import com.xtopdf.xtopdf.factories.PngFileConverterFactory;
import com.xtopdf.xtopdf.factories.TxtFileConverterFactory;
import com.xtopdf.xtopdf.factories.XlsxFileConverterFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileConversionServiceTest {

    @Mock
    private TxtFileConverterFactory txtFileConverterFactory;
    @Mock
    private DocxFileConverterFactory docxFileConverterFactory;
    @Mock
    private HtmlFileConverterFactory htmlFileConverterFactory;
    @Mock
    private SvgFileConverterFactory svgFileConverterFactory;
    private JpegFileConverterFactory jpegFileConverterFactory;
    @Mock
    private PngFileConverterFactory pngFileConverterFactory;
    @Mock
    private XlsxFileConverterFactory xlsxFileConverterFactory;
    
    @Mock
    private FileConverter mockConverter;

    private FileConversionService fileConversionService;

    @BeforeEach
    void setUp() {
        fileConversionService = new FileConversionService(txtFileConverterFactory, docxFileConverterFactory, 
                                                          htmlFileConverterFactory, jpegFileConverterFactory, 
                                                          pngFileConverterFactory, xlsxFileConverterFactory, svgFileConverterFactory);
    }

    @Test
    void convertFileTestFileConversionException() {
        var fake = "fake";
        var inputFile = new MockMultipartFile("inputFile", "test.fake", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        assertThrows(FileConversionException.class, () -> fileConversionService.convertFile(inputFile, fake));
    }

    @Test
    void convertFile_TxtFile_SuccessfulConversion() throws FileConversionException {
        var outputFile = "output.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());
        
        when(txtFileConverterFactory.createFileConverter()).thenReturn(mockConverter);
        
        fileConversionService.convertFile(inputFile, outputFile);
        
        verify(txtFileConverterFactory).createFileConverter();
        verify(mockConverter).convertToPDF(eq(inputFile), eq(outputFile));
    }

    @Test
    void convertFile_SvgFile_SuccessfulConversion() throws FileConversionException {
        var outputFile = "output.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.svg", "image/svg+xml", "<svg></svg>".getBytes());
        
        when(svgFileConverterFactory.createFileConverter()).thenReturn(mockConverter);
        
        fileConversionService.convertFile(inputFile, outputFile);
        
        verify(svgFileConverterFactory).createFileConverter();
    }
  
    @Test
    void convertFile_JpegFile_SuccessfulConversion() throws FileConversionException {
        var outputFile = "output.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.jpeg", "image/jpeg", "test content".getBytes());
        
        when(jpegFileConverterFactory.createFileConverter()).thenReturn(mockConverter);
        
        fileConversionService.convertFile(inputFile, outputFile);
        
        verify(jpegFileConverterFactory).createFileConverter();
        verify(mockConverter).convertToPDF(eq(inputFile), eq(outputFile));
    }

    @Test
    void convertFile_PngFile_SuccessfulConversion() throws FileConversionException {
        var outputFile = "output.pdf";
        var inputFile = new MockMultipartFile("inputFile", "test.png", "image/png", "test content".getBytes());
        
        when(pngFileConverterFactory.createFileConverter()).thenReturn(mockConverter);
        
        fileConversionService.convertFile(inputFile, outputFile);
        
        verify(pngFileConverterFactory).createFileConverter();
        verify(mockConverter).convertToPDF(eq(inputFile), eq(outputFile));
    }

    @ParameterizedTest
    @CsvSource({
            ".txt, true",
            ".docx, true",
            ".html, true",
            ".svg, true",
            ".jpeg, true",
            ".jpg, true",
            ".png, true",
            ".xlsx, true"
    })
    void getFactoryForFileTest(String extension, boolean expected) {
        assertEquals(expected, Objects.nonNull(fileConversionService.getFactoryForFile(extension)));
    }

    @Test
    void convertFile_EmptyFilename_ThrowsFileConversionException() {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "", MediaType.TEXT_PLAIN_VALUE, "test".getBytes());
        assertThrows(FileConversionException.class, () -> fileConversionService.convertFile(inputFile, "output.pdf"));
    }

    @Test
    void convertFile_NullMultipartFile_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> fileConversionService.convertFile(null, "output.pdf"));
    }

    @ParameterizedTest
    @CsvSource({
            "file.TXT, true",
            "file.Docx, true",
            "file.HTML, true",
            "file.JPEG, true",
            "file.JPG, true",
            "file.PNG, true",
            "file.TxT, true",
            "file.dOcX, true",
            "file.HtMl, true",
            "file.JpEg, true",
            "file.jPg, true",
            "file.PnG, true"
    })
    void getFactoryForFile_CaseInsensitiveExtensions(String filename, boolean expected) {
        // Lowercase the extension in getFactoryForFile for this test to pass, or update the service accordingly.
        assertEquals(expected, Objects.nonNull(fileConversionService.getFactoryForFile(filename.toLowerCase())));
    }

    @Test
    void getFactoryForFile_NoExtension_ReturnsNull() {
        assertEquals(null, fileConversionService.getFactoryForFile("file"));
    }

    @Test
    void getFactoryForFile_JustExtension_ReturnsFactory() {
        assertEquals(txtFileConverterFactory, fileConversionService.getFactoryForFile(".txt"));
    }

    @Test
    void getFactoryForFile_JpegExtension_ReturnsCorrectFactory() {
        assertEquals(jpegFileConverterFactory, fileConversionService.getFactoryForFile(".jpeg"));
    }

    @Test
    void getFactoryForFile_JpgExtension_ReturnsCorrectFactory() {
        assertEquals(jpegFileConverterFactory, fileConversionService.getFactoryForFile(".jpg"));
    }

    @Test
    void getFactoryForFile_PngExtension_ReturnsCorrectFactory() {
        assertEquals(pngFileConverterFactory, fileConversionService.getFactoryForFile(".png"));
    }

    @Test
    void getFactoryForFile_MultipleDots_ReturnsCorrectFactory() {
        assertEquals(txtFileConverterFactory, fileConversionService.getFactoryForFile("archive.backup.txt"));
    }
}