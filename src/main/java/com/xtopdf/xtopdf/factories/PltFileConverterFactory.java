package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.PltFileConverter;

@AllArgsConstructor
@Component
public class PltFileConverterFactory implements FileConverterFactory {
    private final PltFileConverter pltFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return pltFileConverter;
    }
}
