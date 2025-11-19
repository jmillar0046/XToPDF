package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.StpFileConverter;
import com.xtopdf.xtopdf.converters.FileConverter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class StpFileConverterFactoryTest {
    @Test
    void testCreateFileConverter() {
        StpFileConverter fileConverter = Mockito.mock(StpFileConverter.class);
        StpFileConverterFactory factory = new StpFileConverterFactory(fileConverter);
        FileConverter converter = factory.createFileConverter();
        assertNotNull(converter);
        assertSame(fileConverter, converter);
    }
}
