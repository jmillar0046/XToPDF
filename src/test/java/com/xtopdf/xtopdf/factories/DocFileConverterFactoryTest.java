package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.DocFileConverter;
import com.xtopdf.xtopdf.converters.FileConverter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class DocFileConverterFactoryTest {
    @Test
    void testCreateFileConverter() {
        DocFileConverter fileConverter = Mockito.mock(DocFileConverter.class);
        DocFileConverterFactory factory = new DocFileConverterFactory(fileConverter);
        FileConverter converter = factory.createFileConverter();
        assertNotNull(converter);
        assertSame(fileConverter, converter);
    }
}
