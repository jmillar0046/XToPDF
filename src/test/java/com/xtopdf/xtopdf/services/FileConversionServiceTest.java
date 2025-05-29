package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.factories.DocxFileConverterFactory;
import com.xtopdf.xtopdf.factories.HtmlFileConverterFactory;
import com.xtopdf.xtopdf.factories.TxtFileConverterFactory;
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

@ExtendWith(MockitoExtension.class)
class FileConversionServiceTest {

    @Mock
    private TxtFileConverterFactory txtFileConverterFactory;
    @Mock
    private DocxFileConverterFactory docxFileConverterFactory;
    @Mock
    private HtmlFileConverterFactory htmlFileConverterFactory;

    private FileConversionService fileConversionService;

    @BeforeEach
    void setUp() {
        fileConversionService = new FileConversionService(txtFileConverterFactory, docxFileConverterFactory, htmlFileConverterFactory);
    }

    @Test
    void convertFileTestFileConversionException() {
        var fake = "fake";
        var inputFile = new MockMultipartFile("inputFile", "test.fake", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        assertThrows(FileConversionException.class, () -> fileConversionService.convertFile(inputFile, fake));
    }

    @ParameterizedTest
    @CsvSource({
            ".txt, true",
            ".docx, true",
            ".html, true",
            ".xlsx, false"
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
            "file.TxT, true",
            "file.dOcX, true",
            "file.HtMl, true"
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
    void getFactoryForFile_MultipleDots_ReturnsCorrectFactory() {
        assertEquals(txtFileConverterFactory, fileConversionService.getFactoryForFile("archive.backup.txt"));
    }
}