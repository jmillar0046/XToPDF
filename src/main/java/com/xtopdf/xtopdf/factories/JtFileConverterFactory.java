package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.JtFileConverter;

@AllArgsConstructor
@Component
public class JtFileConverterFactory implements FileConverterFactory {
    private final JtFileConverter jtFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return jtFileConverter;
    }
}
