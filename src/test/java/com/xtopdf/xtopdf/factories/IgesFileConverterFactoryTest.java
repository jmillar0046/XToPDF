package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.IgesFileConverter;
import com.xtopdf.xtopdf.converters.FileConverter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class IgesFileConverterFactoryTest {
    @Test
    void testCreateFileConverter() {
        IgesFileConverter fileConverter = Mockito.mock(IgesFileConverter.class);
        IgesFileConverterFactory factory = new IgesFileConverterFactory(fileConverter);
        FileConverter converter = factory.createFileConverter();
        assertNotNull(converter);
        assertSame(fileConverter, converter);
    }
}
