package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.PptFileConverter;

@AllArgsConstructor
@Component
public class PptFileConverterFactory implements FileConverterFactory {
    private final PptFileConverter pptFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return pptFileConverter;
    }
}
