package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.RtfFileConverter;
import com.xtopdf.xtopdf.converters.FileConverter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class RtfFileConverterFactoryTest {
    @Test
    void testCreateFileConverter() {
        RtfFileConverter fileConverter = Mockito.mock(RtfFileConverter.class);
        RtfFileConverterFactory factory = new RtfFileConverterFactory(fileConverter);
        FileConverter converter = factory.createFileConverter();
        assertNotNull(converter);
        assertSame(fileConverter, converter);
    }
}
