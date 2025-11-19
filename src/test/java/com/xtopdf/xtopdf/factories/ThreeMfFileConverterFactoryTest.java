package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.ThreeMfFileConverter;
import com.xtopdf.xtopdf.converters.FileConverter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ThreeMfFileConverterFactoryTest {
    @Test
    void testCreateFileConverter() {
        ThreeMfFileConverter fileConverter = Mockito.mock(ThreeMfFileConverter.class);
        ThreeMfFileConverterFactory factory = new ThreeMfFileConverterFactory(fileConverter);
        FileConverter converter = factory.createFileConverter();
        assertNotNull(converter);
        assertSame(fileConverter, converter);
    }
}
