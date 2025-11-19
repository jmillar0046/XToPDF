package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.DwtFileConverter;
import com.xtopdf.xtopdf.converters.FileConverter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class DwtFileConverterFactoryTest {
    @Test
    void testCreateFileConverter() {
        DwtFileConverter fileConverter = Mockito.mock(DwtFileConverter.class);
        DwtFileConverterFactory factory = new DwtFileConverterFactory(fileConverter);
        FileConverter converter = factory.createFileConverter();
        assertNotNull(converter);
        assertSame(fileConverter, converter);
    }
}
