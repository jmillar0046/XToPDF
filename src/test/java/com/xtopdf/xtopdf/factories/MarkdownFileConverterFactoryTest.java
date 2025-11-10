package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.MarkdownFileConverter;
import com.xtopdf.xtopdf.services.MarkdownToPdfService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import com.xtopdf.xtopdf.services.PageNumberService;
import org.mockito.Mockito;

class MarkdownFileConverterFactoryTest {

    @Mock
    private MarkdownToPdfService markdownToPdfService;

    private final MarkdownFileConverter markdownFileConverter = new MarkdownFileConverter(markdownToPdfService, Mockito.mock(PageNumberService.class));
    private final MarkdownFileConverterFactory markdownFileConverterFactory = new MarkdownFileConverterFactory(markdownFileConverter);

    @Test
    void testCreateMarkdownFileConverter() {
        assertInstanceOf(MarkdownFileConverter.class, markdownFileConverterFactory.createFileConverter());
    }
}
