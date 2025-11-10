package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.JsonFileConverter;

@AllArgsConstructor
@Component
public class JsonFileConverterFactory implements FileConverterFactory {
    private final JsonFileConverter jsonFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return jsonFileConverter;
    }
}
