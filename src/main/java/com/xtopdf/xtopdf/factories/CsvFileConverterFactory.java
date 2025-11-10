package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.CsvFileConverter;

@AllArgsConstructor
@Component
public class CsvFileConverterFactory implements FileConverterFactory {
    private final CsvFileConverter csvFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return csvFileConverter;
    }
}
