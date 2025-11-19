package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.ObjFileConverter;
import com.xtopdf.xtopdf.converters.FileConverter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ObjFileConverterFactoryTest {
    @Test
    void testCreateFileConverter() {
        ObjFileConverter fileConverter = Mockito.mock(ObjFileConverter.class);
        ObjFileConverterFactory factory = new ObjFileConverterFactory(fileConverter);
        FileConverter converter = factory.createFileConverter();
        assertNotNull(converter);
        assertSame(fileConverter, converter);
    }
}
