package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.OdsFileConverter;

@AllArgsConstructor
@Component
public class OdsFileConverterFactory implements FileConverterFactory {
    private final OdsFileConverter odsFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return odsFileConverter;
    }
}
