package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.IgesFileConverter;

@AllArgsConstructor
@Component
public class IgesFileConverterFactory implements FileConverterFactory {
    private final IgesFileConverter igesFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return igesFileConverter;
    }
}
