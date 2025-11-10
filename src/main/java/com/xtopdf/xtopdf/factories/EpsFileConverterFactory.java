package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.EpsFileConverter;

@AllArgsConstructor
@Component
public class EpsFileConverterFactory implements FileConverterFactory {
    private final EpsFileConverter epsFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return epsFileConverter;
    }
}
