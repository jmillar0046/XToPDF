package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.DwfFileConverter;
import com.xtopdf.xtopdf.converters.FileConverter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class DwfFileConverterFactoryTest {
    @Test
    void testCreateFileConverter() {
        DwfFileConverter fileConverter = Mockito.mock(DwfFileConverter.class);
        DwfFileConverterFactory factory = new DwfFileConverterFactory(fileConverter);
        FileConverter converter = factory.createFileConverter();
        assertNotNull(converter);
        assertSame(fileConverter, converter);
    }
}
