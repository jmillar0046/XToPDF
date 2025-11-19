package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.HpglFileConverter;
import com.xtopdf.xtopdf.converters.FileConverter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class HpglFileConverterFactoryTest {
    @Test
    void testCreateFileConverter() {
        HpglFileConverter fileConverter = Mockito.mock(HpglFileConverter.class);
        HpglFileConverterFactory factory = new HpglFileConverterFactory(fileConverter);
        FileConverter converter = factory.createFileConverter();
        assertNotNull(converter);
        assertSame(fileConverter, converter);
    }
}
