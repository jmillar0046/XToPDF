package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.StlFileConverter;
import com.xtopdf.xtopdf.converters.FileConverter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class StlFileConverterFactoryTest {
    @Test
    void testCreateFileConverter() {
        StlFileConverter fileConverter = Mockito.mock(StlFileConverter.class);
        StlFileConverterFactory factory = new StlFileConverterFactory(fileConverter);
        FileConverter converter = factory.createFileConverter();
        assertNotNull(converter);
        assertSame(fileConverter, converter);
    }
}
