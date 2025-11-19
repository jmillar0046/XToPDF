package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.SldprtFileConverter;

@AllArgsConstructor
@Component
public class SldprtFileConverterFactory implements FileConverterFactory {
    private final SldprtFileConverter sldprtFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return sldprtFileConverter;
    }
}
