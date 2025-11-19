package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.SkpFileConverter;

@AllArgsConstructor
@Component
public class SkpFileConverterFactory implements FileConverterFactory {
    private final SkpFileConverter skpFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return skpFileConverter;
    }
}
