package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.IgsFileConverter;

@AllArgsConstructor
@Component
public class IgsFileConverterFactory implements FileConverterFactory {
    private final IgsFileConverter igsFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return igsFileConverter;
    }
}
