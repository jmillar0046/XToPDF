package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.IgsFileConverter;
import com.xtopdf.xtopdf.converters.FileConverter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class IgsFileConverterFactoryTest {
    @Test
    void testCreateFileConverter() {
        IgsFileConverter fileConverter = Mockito.mock(IgsFileConverter.class);
        IgsFileConverterFactory factory = new IgsFileConverterFactory(fileConverter);
        FileConverter converter = factory.createFileConverter();
        assertNotNull(converter);
        assertSame(fileConverter, converter);
    }
}
