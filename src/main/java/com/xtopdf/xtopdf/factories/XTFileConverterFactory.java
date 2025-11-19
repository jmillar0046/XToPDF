package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.XTFileConverter;

@AllArgsConstructor
@Component
public class XTFileConverterFactory implements FileConverterFactory {
    private final XTFileConverter xtFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return xtFileConverter;
    }
}
