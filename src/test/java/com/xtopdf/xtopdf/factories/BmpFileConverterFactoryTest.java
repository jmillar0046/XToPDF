package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.BmpFileConverter;
import com.xtopdf.xtopdf.converters.FileConverter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class BmpFileConverterFactoryTest {
    @Test
    void testCreateFileConverter() {
        BmpFileConverter fileConverter = Mockito.mock(BmpFileConverter.class);
        BmpFileConverterFactory factory = new BmpFileConverterFactory(fileConverter);
        FileConverter converter = factory.createFileConverter();
        assertNotNull(converter);
        assertSame(fileConverter, converter);
    }
}
