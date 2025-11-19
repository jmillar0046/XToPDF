package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.ThreeDmFileConverter;

@AllArgsConstructor
@Component
public class ThreeDmFileConverterFactory implements FileConverterFactory {
    private final ThreeDmFileConverter threedmFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return threedmFileConverter;
    }
}
