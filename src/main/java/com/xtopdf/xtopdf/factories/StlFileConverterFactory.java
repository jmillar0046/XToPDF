package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.StlFileConverter;

@AllArgsConstructor
@Component
public class StlFileConverterFactory implements FileConverterFactory {
    private final StlFileConverter stlFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return stlFileConverter;
    }
}
