package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.F3dFileConverter;

@AllArgsConstructor
@Component
public class F3dFileConverterFactory implements FileConverterFactory {
    private final F3dFileConverter f3dFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return f3dFileConverter;
    }
}
