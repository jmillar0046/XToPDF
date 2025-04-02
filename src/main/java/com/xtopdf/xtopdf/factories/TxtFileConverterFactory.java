package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.TxtFileConverter;

@AllArgsConstructor
@Component
public class TxtFileConverterFactory implements FileConverterFactory {
    private final TxtFileConverter txtFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return txtFileConverter;
    }
    
}
