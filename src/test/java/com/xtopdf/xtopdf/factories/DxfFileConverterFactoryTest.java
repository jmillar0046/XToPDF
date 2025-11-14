package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.DxfFileConverter;
import com.xtopdf.xtopdf.converters.FileConverter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class DxfFileConverterFactoryTest {

    @Test
    void testCreateFileConverter() {
        DxfFileConverter dxfFileConverter = Mockito.mock(DxfFileConverter.class);
        DxfFileConverterFactory factory = new DxfFileConverterFactory(dxfFileConverter);

        FileConverter converter = factory.createFileConverter();

        assertNotNull(converter);
        assertSame(dxfFileConverter, converter);
    }
}
