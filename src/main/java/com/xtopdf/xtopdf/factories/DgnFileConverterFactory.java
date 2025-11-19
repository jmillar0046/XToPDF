package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.DgnFileConverter;

@AllArgsConstructor
@Component
public class DgnFileConverterFactory implements FileConverterFactory {
    private final DgnFileConverter dgnFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return dgnFileConverter;
    }
}
