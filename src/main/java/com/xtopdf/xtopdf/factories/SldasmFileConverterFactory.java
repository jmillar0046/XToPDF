package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.SldasmFileConverter;

@AllArgsConstructor
@Component
public class SldasmFileConverterFactory implements FileConverterFactory {
    private final SldasmFileConverter sldasmFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return sldasmFileConverter;
    }
}
