package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.CsvFileConverter;
import com.xtopdf.xtopdf.services.CsvToPdfService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import com.xtopdf.xtopdf.services.PageNumberService;
import org.mockito.Mockito;

class CsvFileConverterFactoryTest {

    @Mock
    private CsvToPdfService csvToPdfService;

    private final CsvFileConverter csvFileConverter = new CsvFileConverter(csvToPdfService, Mockito.mock(PageNumberService.class));
    private final CsvFileConverterFactory csvFileConverterFactory = new CsvFileConverterFactory(csvFileConverter);

    @Test
    void testCreateCsvFileConverter() {
        assertInstanceOf(CsvFileConverter.class, csvFileConverterFactory.createFileConverter());
    }
}
