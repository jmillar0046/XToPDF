package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.OdpFileConverter;
import com.xtopdf.xtopdf.converters.FileConverter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class OdpFileConverterFactoryTest {
    @Test
    void testCreateFileConverter() {
        OdpFileConverter fileConverter = Mockito.mock(OdpFileConverter.class);
        OdpFileConverterFactory factory = new OdpFileConverterFactory(fileConverter);
        FileConverter converter = factory.createFileConverter();
        assertNotNull(converter);
        assertSame(fileConverter, converter);
    }
}
