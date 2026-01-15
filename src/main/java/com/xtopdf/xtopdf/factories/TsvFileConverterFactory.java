package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.TsvFileConverter;

@AllArgsConstructor
@Component
public class TsvFileConverterFactory implements FileConverterFactory {
    private final TsvFileConverter tsvFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return tsvFileConverter;
    }
}
