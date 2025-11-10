package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.TxtFileConverter;
import com.xtopdf.xtopdf.services.TxtToPdfService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import com.xtopdf.xtopdf.services.PageNumberService;
import org.mockito.Mockito;

class TxtFileConverterFactoryTest {

    @Mock
    private TxtToPdfService txtToPdfService;

    private final TxtFileConverter txtFileConverter = new TxtFileConverter(txtToPdfService, Mockito.mock(PageNumberService.class));
    private final TxtFileConverterFactory txtFileConverterFactory = new TxtFileConverterFactory(txtFileConverter);

    @Test
    void testCreateTxtFileConverter() {
        assertInstanceOf(TxtFileConverter.class, txtFileConverterFactory.createFileConverter());
    }
}