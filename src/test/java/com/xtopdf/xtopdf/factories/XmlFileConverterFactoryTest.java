package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.XmlFileConverter;
import com.xtopdf.xtopdf.services.XmlToPdfService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import com.xtopdf.xtopdf.services.PageNumberService;
import org.mockito.Mockito;

class XmlFileConverterFactoryTest {

    @Mock
    private XmlToPdfService xmlToPdfService;

    private final XmlFileConverter xmlFileConverter = new XmlFileConverter(xmlToPdfService, Mockito.mock(PageNumberService.class));
    private final XmlFileConverterFactory xmlFileConverterFactory = new XmlFileConverterFactory(xmlFileConverter);

    @Test
    void testCreateXmlFileConverter() {
        assertInstanceOf(XmlFileConverter.class, xmlFileConverterFactory.createFileConverter());
    }
}
