package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.F3zFileConverter;

@AllArgsConstructor
@Component
public class F3zFileConverterFactory implements FileConverterFactory {
    private final F3zFileConverter f3zFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return f3zFileConverter;
    }
}
