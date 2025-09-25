package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.PptxFileConverter;

@AllArgsConstructor
@Component
public class PptxFileConverterFactory implements FileConverterFactory {
    private final PptxFileConverter pptxFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return pptxFileConverter;
    }
}