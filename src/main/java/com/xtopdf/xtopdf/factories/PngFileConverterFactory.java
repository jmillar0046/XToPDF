package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.PngFileConverter;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class PngFileConverterFactory implements FileConverterFactory {
    private final PngFileConverter pngFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return pngFileConverter;
    }
}