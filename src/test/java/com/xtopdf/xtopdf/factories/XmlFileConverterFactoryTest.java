package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.XmlFileConverter;
import com.xtopdf.xtopdf.services.XmlToPdfService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class XmlFileConverterFactoryTest {

    @Mock
    private XmlToPdfService xmlToPdfService;

    private final XmlFileConverter xmlFileConverter = new XmlFileConverter(xmlToPdfService);
    private final XmlFileConverterFactory xmlFileConverterFactory = new XmlFileConverterFactory(xmlFileConverter);

    @Test
    void testCreateXmlFileConverter() {
        assertInstanceOf(XmlFileConverter.class, xmlFileConverterFactory.createFileConverter());
    }
}
