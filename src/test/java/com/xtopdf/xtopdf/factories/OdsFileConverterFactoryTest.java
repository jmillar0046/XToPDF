package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.OdsFileConverter;
import com.xtopdf.xtopdf.converters.FileConverter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class OdsFileConverterFactoryTest {
    @Test
    void testCreateFileConverter() {
        OdsFileConverter fileConverter = Mockito.mock(OdsFileConverter.class);
        OdsFileConverterFactory factory = new OdsFileConverterFactory(fileConverter);
        FileConverter converter = factory.createFileConverter();
        assertNotNull(converter);
        assertSame(fileConverter, converter);
    }
}
