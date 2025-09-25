package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.RtfFileConverter;
import com.xtopdf.xtopdf.services.RtfToPdfService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class RtfFileConverterFactoryTest {

    @Mock
    private RtfToPdfService rtfToPdfService;

    private final RtfFileConverter rtfFileConverter = new RtfFileConverter(rtfToPdfService);
    private final RtfFileConverterFactory rtfFileConverterFactory = new RtfFileConverterFactory(rtfFileConverter);

    @Test
    void testCreateRtfFileConverter() {
        assertInstanceOf(RtfFileConverter.class, rtfFileConverterFactory.createFileConverter());
    }
}