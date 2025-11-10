package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.TiffFileConverter;
import com.xtopdf.xtopdf.services.TiffToPdfService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import com.xtopdf.xtopdf.services.PageNumberService;
import org.mockito.Mockito;

class TiffFileConverterFactoryTest {

    @Mock
    private TiffToPdfService tiffToPdfService;

    private final TiffFileConverter tiffFileConverter = new TiffFileConverter(tiffToPdfService, Mockito.mock(PageNumberService.class));
    private final TiffFileConverterFactory tiffFileConverterFactory = new TiffFileConverterFactory(tiffFileConverter);

    @Test
    void testCreateTiffFileConverter() {
        assertInstanceOf(TiffFileConverter.class, tiffFileConverterFactory.createFileConverter());
    }
}