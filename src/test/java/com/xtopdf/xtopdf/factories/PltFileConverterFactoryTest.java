package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.PltFileConverter;
import com.xtopdf.xtopdf.converters.FileConverter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class PltFileConverterFactoryTest {
    @Test
    void testCreateFileConverter() {
        PltFileConverter fileConverter = Mockito.mock(PltFileConverter.class);
        PltFileConverterFactory factory = new PltFileConverterFactory(fileConverter);
        FileConverter converter = factory.createFileConverter();
        assertNotNull(converter);
        assertSame(fileConverter, converter);
    }
}
