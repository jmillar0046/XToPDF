package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.StpFileConverter;

@AllArgsConstructor
@Component
public class StpFileConverterFactory implements FileConverterFactory {
    private final StpFileConverter stpFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return stpFileConverter;
    }
}
