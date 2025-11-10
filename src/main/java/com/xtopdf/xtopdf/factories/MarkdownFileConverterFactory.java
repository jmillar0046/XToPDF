package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.MarkdownFileConverter;

@AllArgsConstructor
@Component
public class MarkdownFileConverterFactory implements FileConverterFactory {
    private final MarkdownFileConverter markdownFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return markdownFileConverter;
    }
    
}
