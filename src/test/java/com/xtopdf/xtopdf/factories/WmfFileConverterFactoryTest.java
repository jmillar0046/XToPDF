package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.WmfFileConverter;
import com.xtopdf.xtopdf.converters.FileConverter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class WmfFileConverterFactoryTest {
    @Test
    void testCreateFileConverter() {
        WmfFileConverter fileConverter = Mockito.mock(WmfFileConverter.class);
        WmfFileConverterFactory factory = new WmfFileConverterFactory(fileConverter);
        FileConverter converter = factory.createFileConverter();
        assertNotNull(converter);
        assertSame(fileConverter, converter);
    }
}
