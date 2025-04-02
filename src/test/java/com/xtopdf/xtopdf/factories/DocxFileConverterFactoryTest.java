package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.DocxFileConverter;
import com.xtopdf.xtopdf.services.DocxToPdfService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;

class DocxFileConverterFactoryTest {
    @Mock
    private DocxToPdfService docxToPdfService;

    private final DocxFileConverter docxFileConverter = new DocxFileConverter(docxToPdfService);
    private final DocxFileConverterFactory docxFileConverterFactory = new DocxFileConverterFactory(docxFileConverter);

    @Test
    void testCreateFileConverter() {
        assertInstanceOf(DocxFileConverter.class, docxFileConverterFactory.createFileConverter());
    }
}