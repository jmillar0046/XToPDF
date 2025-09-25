package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.SvgFileConverter;
import com.xtopdf.xtopdf.services.SvgToPdfService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class SvgFileConverterFactoryTest {
    @Mock
    private SvgToPdfService svgToPdfService;

    private final SvgFileConverter svgFileConverter = new SvgFileConverter(svgToPdfService);
    private final SvgFileConverterFactory svgFileConverterFactory = new SvgFileConverterFactory(svgFileConverter);

    @Test
    void testCreateFileConverter() {
        assertInstanceOf(SvgFileConverter.class, svgFileConverterFactory.createFileConverter());
    }
}