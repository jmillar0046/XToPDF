package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.DocFileConverter;

@AllArgsConstructor
@Component
public class DocFileConverterFactory implements FileConverterFactory {
    private final DocFileConverter docFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return docFileConverter;
    }
}
