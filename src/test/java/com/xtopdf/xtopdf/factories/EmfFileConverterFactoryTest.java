package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.EmfFileConverter;
import com.xtopdf.xtopdf.converters.FileConverter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class EmfFileConverterFactoryTest {
    @Test
    void testCreateFileConverter() {
        EmfFileConverter fileConverter = Mockito.mock(EmfFileConverter.class);
        EmfFileConverterFactory factory = new EmfFileConverterFactory(fileConverter);
        FileConverter converter = factory.createFileConverter();
        assertNotNull(converter);
        assertSame(fileConverter, converter);
    }
}
