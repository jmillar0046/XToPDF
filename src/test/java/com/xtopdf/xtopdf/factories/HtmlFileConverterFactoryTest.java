package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.HtmlFileConverter;
import com.xtopdf.xtopdf.services.HtmlToPdfService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class HtmlFileConverterFactoryTest {
    @Mock
    private HtmlToPdfService htmlToPdfService;

    private final HtmlFileConverter htmlFileConverter = new HtmlFileConverter(htmlToPdfService);
    private final HtmlFileConverterFactory htmlFileConverterFactory = new HtmlFileConverterFactory(htmlFileConverter);

    @Test
    void testCreateFileConverter() {
        assertInstanceOf(HtmlFileConverter.class, htmlFileConverterFactory.createFileConverter());
    }
}