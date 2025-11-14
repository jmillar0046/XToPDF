package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.PptFileConverter;
import com.xtopdf.xtopdf.converters.FileConverter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class PptFileConverterFactoryTest {
    @Test
    void testCreateFileConverter() {
        PptFileConverter fileConverter = Mockito.mock(PptFileConverter.class);
        PptFileConverterFactory factory = new PptFileConverterFactory(fileConverter);
        FileConverter converter = factory.createFileConverter();
        assertNotNull(converter);
        assertSame(fileConverter, converter);
    }
}
