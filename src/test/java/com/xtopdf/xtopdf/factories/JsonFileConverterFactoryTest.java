package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.JsonFileConverter;
import com.xtopdf.xtopdf.services.JsonToPdfService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class JsonFileConverterFactoryTest {

    @Mock
    private JsonToPdfService jsonToPdfService;

    private final JsonFileConverter jsonFileConverter = new JsonFileConverter(jsonToPdfService);
    private final JsonFileConverterFactory jsonFileConverterFactory = new JsonFileConverterFactory(jsonFileConverter);

    @Test
    void testCreateJsonFileConverter() {
        assertInstanceOf(JsonFileConverter.class, jsonFileConverterFactory.createFileConverter());
    }
}
