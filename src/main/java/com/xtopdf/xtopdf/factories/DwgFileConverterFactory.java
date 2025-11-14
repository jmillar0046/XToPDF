package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.DwgFileConverter;

@AllArgsConstructor
@Component
public class DwgFileConverterFactory implements FileConverterFactory {
    private final DwgFileConverter dwgFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return dwgFileConverter;
    }
    
}
