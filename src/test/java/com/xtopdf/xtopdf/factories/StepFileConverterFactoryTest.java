package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.StepFileConverter;
import com.xtopdf.xtopdf.converters.FileConverter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class StepFileConverterFactoryTest {
    @Test
    void testCreateFileConverter() {
        StepFileConverter fileConverter = Mockito.mock(StepFileConverter.class);
        StepFileConverterFactory factory = new StepFileConverterFactory(fileConverter);
        FileConverter converter = factory.createFileConverter();
        assertNotNull(converter);
        assertSame(fileConverter, converter);
    }
}
