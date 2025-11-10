package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.JpegFileConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import com.xtopdf.xtopdf.services.PageNumberService;
import org.mockito.Mockito;

@ExtendWith(MockitoExtension.class)
class JpegFileConverterFactoryTest {

    @Mock
    private JpegFileConverter jpegFileConverter;

    private JpegFileConverterFactory jpegFileConverterFactory;

    @BeforeEach
    void setUp() {
        jpegFileConverterFactory = new JpegFileConverterFactory(jpegFileConverter);
    }

    @Test
    void createFileConverter_ReturnsJpegFileConverter() {
        FileConverter result = jpegFileConverterFactory.createFileConverter();

        assertNotNull(result);
        assertEquals(jpegFileConverter, result);
    }

    @Test
    void createFileConverter_ReturnsInstanceOfFileConverter() {
        FileConverter result = jpegFileConverterFactory.createFileConverter();

        assertInstanceOf(FileConverter.class, result);
    }
}