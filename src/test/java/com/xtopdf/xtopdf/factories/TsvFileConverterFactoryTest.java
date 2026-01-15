package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.TsvFileConverter;
import com.xtopdf.xtopdf.services.TsvToPdfService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TsvFileConverterFactoryTest {

    @Mock
    private TsvToPdfService tsvToPdfService;

    private final TsvFileConverter tsvFileConverter = new TsvFileConverter(tsvToPdfService);
    private final TsvFileConverterFactory tsvFileConverterFactory = new TsvFileConverterFactory(tsvFileConverter);

    @Test
    void testCreateTsvFileConverter() {
        assertInstanceOf(TsvFileConverter.class, tsvFileConverterFactory.createFileConverter());
    }

    @Test
    void testCreateTsvFileConverter_NotNull() {
        assertNotNull(tsvFileConverterFactory.createFileConverter());
    }
}
