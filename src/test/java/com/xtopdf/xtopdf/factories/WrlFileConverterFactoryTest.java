package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.WrlFileConverter;
import com.xtopdf.xtopdf.converters.FileConverter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class WrlFileConverterFactoryTest {
    @Test
    void testCreateFileConverter() {
        WrlFileConverter fileConverter = Mockito.mock(WrlFileConverter.class);
        WrlFileConverterFactory factory = new WrlFileConverterFactory(fileConverter);
        FileConverter converter = factory.createFileConverter();
        assertNotNull(converter);
        assertSame(fileConverter, converter);
    }
}
