package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.X3dFileConverter;

@AllArgsConstructor
@Component
public class X3dFileConverterFactory implements FileConverterFactory {
    private final X3dFileConverter x3dFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return x3dFileConverter;
    }
}
