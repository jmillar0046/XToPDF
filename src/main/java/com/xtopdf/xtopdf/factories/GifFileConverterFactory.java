package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.GifFileConverter;

@AllArgsConstructor
@Component
public class GifFileConverterFactory implements FileConverterFactory {
    private final GifFileConverter gifFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return gifFileConverter;
    }
}
