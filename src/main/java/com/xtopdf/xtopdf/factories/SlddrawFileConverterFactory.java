package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.SlddrawFileConverter;

@AllArgsConstructor
@Component
public class SlddrawFileConverterFactory implements FileConverterFactory {
    private final SlddrawFileConverter slddrawFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return slddrawFileConverter;
    }
}
