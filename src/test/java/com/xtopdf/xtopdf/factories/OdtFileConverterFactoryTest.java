package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.OdtFileConverter;
import com.xtopdf.xtopdf.converters.FileConverter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class OdtFileConverterFactoryTest {
    @Test
    void testCreateFileConverter() {
        OdtFileConverter fileConverter = Mockito.mock(OdtFileConverter.class);
        OdtFileConverterFactory factory = new OdtFileConverterFactory(fileConverter);
        FileConverter converter = factory.createFileConverter();
        assertNotNull(converter);
        assertSame(fileConverter, converter);
    }
}
