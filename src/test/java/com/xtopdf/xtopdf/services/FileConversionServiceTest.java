package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.factories.DocxFileConverterFactory;
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

    private FileConversionService fileConversionService;

    @BeforeEach
    void setUp() {
        fileConversionService = new FileConversionService(txtFileConverterFactory, docxFileConverterFactory);
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
            ".fake, false"
    })
    void getFactoryForFileTest(String extension, boolean expected) {
        assertEquals(expected, Objects.nonNull(fileConversionService.getFactoryForFile(extension)));
    }
}