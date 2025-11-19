package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.DwtFileConverter;

@AllArgsConstructor
@Component
public class DwtFileConverterFactory implements FileConverterFactory {
    private final DwtFileConverter dwtFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return dwtFileConverter;
    }
}
