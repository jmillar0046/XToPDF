package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.WrlFileConverter;

@AllArgsConstructor
@Component
public class WrlFileConverterFactory implements FileConverterFactory {
    private final WrlFileConverter wrlFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return wrlFileConverter;
    }
}
