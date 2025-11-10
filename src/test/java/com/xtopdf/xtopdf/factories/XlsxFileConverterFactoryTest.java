package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.XlsxFileConverter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.xtopdf.xtopdf.services.PageNumberService;

class XlsxFileConverterFactoryTest {

    @Test
    void createFileConverter_ReturnsXlsxFileConverter() {
        XlsxFileConverter xlsxFileConverter = Mockito.mock(XlsxFileConverter.class);
        XlsxFileConverterFactory factory = new XlsxFileConverterFactory(xlsxFileConverter);

        FileConverter result = factory.createFileConverter();

        assertEquals(xlsxFileConverter, result);
    }
}