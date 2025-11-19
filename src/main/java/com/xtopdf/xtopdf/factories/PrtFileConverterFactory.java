package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.PrtFileConverter;

@AllArgsConstructor
@Component
public class PrtFileConverterFactory implements FileConverterFactory {
    private final PrtFileConverter prtFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return prtFileConverter;
    }
}
