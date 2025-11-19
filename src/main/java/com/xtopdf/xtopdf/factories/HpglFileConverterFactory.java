package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.HpglFileConverter;

@AllArgsConstructor
@Component
public class HpglFileConverterFactory implements FileConverterFactory {
    private final HpglFileConverter hpglFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return hpglFileConverter;
    }
}
