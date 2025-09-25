package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.BmpFileConverter;
import com.xtopdf.xtopdf.services.BmpToPdfService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class BmpFileConverterFactoryTest {
    @Mock
    private BmpToPdfService bmpToPdfService;

    private final BmpFileConverter bmpFileConverter = new BmpFileConverter(bmpToPdfService);
    private final BmpFileConverterFactory bmpFileConverterFactory = new BmpFileConverterFactory(bmpFileConverter);

    @Test
    void testCreateFileConverter() {
        assertInstanceOf(BmpFileConverter.class, bmpFileConverterFactory.createFileConverter());
    }
}