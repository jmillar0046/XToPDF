package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.GifFileConverter;
import com.xtopdf.xtopdf.services.GifToPdfService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import com.xtopdf.xtopdf.services.PageNumberService;
import org.mockito.Mockito;

class GifFileConverterFactoryTest {

    @Mock
    private GifToPdfService gifToPdfService;

    private final GifFileConverter gifFileConverter = new GifFileConverter(gifToPdfService, Mockito.mock(PageNumberService.class));
    private final GifFileConverterFactory gifFileConverterFactory = new GifFileConverterFactory(gifFileConverter);

    @Test
    void testCreateGifFileConverter() {
        assertInstanceOf(GifFileConverter.class, gifFileConverterFactory.createFileConverter());
    }
}
