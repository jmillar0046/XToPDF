package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.DwfxFileConverter;

@AllArgsConstructor
@Component
public class DwfxFileConverterFactory implements FileConverterFactory {
    private final DwfxFileConverter dwfxFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return dwfxFileConverter;
    }
}
