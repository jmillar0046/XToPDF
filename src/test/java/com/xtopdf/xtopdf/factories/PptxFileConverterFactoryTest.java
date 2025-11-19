package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.PptxFileConverter;
import com.xtopdf.xtopdf.converters.FileConverter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class PptxFileConverterFactoryTest {
    @Test
    void testCreateFileConverter() {
        PptxFileConverter fileConverter = Mockito.mock(PptxFileConverter.class);
        PptxFileConverterFactory factory = new PptxFileConverterFactory(fileConverter);
        FileConverter converter = factory.createFileConverter();
        assertNotNull(converter);
        assertSame(fileConverter, converter);
    }
}
