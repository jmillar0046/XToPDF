package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.AsmFileConverter;

@AllArgsConstructor
@Component
public class AsmFileConverterFactory implements FileConverterFactory {
    private final AsmFileConverter asmFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return asmFileConverter;
    }
}
