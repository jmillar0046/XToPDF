package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.EpubFileConverter;

@AllArgsConstructor
@Component
public class EpubFileConverterFactory implements FileConverterFactory {
    private final EpubFileConverter epubFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return epubFileConverter;
    }
}
