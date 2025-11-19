package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.CatpartFileConverter;

@AllArgsConstructor
@Component
public class CatpartFileConverterFactory implements FileConverterFactory {
    private final CatpartFileConverter catpartFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return catpartFileConverter;
    }
}
