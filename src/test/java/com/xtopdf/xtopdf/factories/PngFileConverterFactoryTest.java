package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.PngFileConverter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PngFileConverterFactoryTest {

    @Test
    void testCreateFileConverter() {
        PngFileConverter mockPngFileConverter = Mockito.mock(PngFileConverter.class);
        PngFileConverterFactory factory = new PngFileConverterFactory(mockPngFileConverter);

        FileConverter result = factory.createFileConverter();

        assertNotNull(result, "FileConverter should not be null");
        assertEquals(mockPngFileConverter, result, "Should return the injected PngFileConverter");
    }
}