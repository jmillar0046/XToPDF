package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.X3dFileConverter;
import com.xtopdf.xtopdf.converters.FileConverter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class X3dFileConverterFactoryTest {
    @Test
    void testCreateFileConverter() {
        X3dFileConverter fileConverter = Mockito.mock(X3dFileConverter.class);
        X3dFileConverterFactory factory = new X3dFileConverterFactory(fileConverter);
        FileConverter converter = factory.createFileConverter();
        assertNotNull(converter);
        assertSame(fileConverter, converter);
    }
}
