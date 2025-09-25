package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.TiffFileConverter;

@AllArgsConstructor
@Component
public class TiffFileConverterFactory implements FileConverterFactory {
    private final TiffFileConverter tiffFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return tiffFileConverter;
    }
    
}