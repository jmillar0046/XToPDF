package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.DwgFileConverter;
import com.xtopdf.xtopdf.converters.FileConverter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class DwgFileConverterFactoryTest {

    @Test
    void testCreateFileConverter() {
        DwgFileConverter dwgFileConverter = Mockito.mock(DwgFileConverter.class);
        DwgFileConverterFactory factory = new DwgFileConverterFactory(dwgFileConverter);

        FileConverter converter = factory.createFileConverter();

        assertNotNull(converter);
        assertSame(dwgFileConverter, converter);
    }
}
