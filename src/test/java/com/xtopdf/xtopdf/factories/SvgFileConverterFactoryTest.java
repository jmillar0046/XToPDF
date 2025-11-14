package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.SvgFileConverter;
import com.xtopdf.xtopdf.converters.FileConverter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class SvgFileConverterFactoryTest {
    @Test
    void testCreateFileConverter() {
        SvgFileConverter fileConverter = Mockito.mock(SvgFileConverter.class);
        SvgFileConverterFactory factory = new SvgFileConverterFactory(fileConverter);
        FileConverter converter = factory.createFileConverter();
        assertNotNull(converter);
        assertSame(fileConverter, converter);
    }
}
