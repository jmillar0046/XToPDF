package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.OdtFileConverter;

@AllArgsConstructor
@Component
public class OdtFileConverterFactory implements FileConverterFactory {
    private final OdtFileConverter odtFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return odtFileConverter;
    }
}
