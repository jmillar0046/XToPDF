package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.DwfxFileConverter;
import com.xtopdf.xtopdf.converters.FileConverter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class DwfxFileConverterFactoryTest {
    @Test
    void testCreateFileConverter() {
        DwfxFileConverter fileConverter = Mockito.mock(DwfxFileConverter.class);
        DwfxFileConverterFactory factory = new DwfxFileConverterFactory(fileConverter);
        FileConverter converter = factory.createFileConverter();
        assertNotNull(converter);
        assertSame(fileConverter, converter);
    }
}
