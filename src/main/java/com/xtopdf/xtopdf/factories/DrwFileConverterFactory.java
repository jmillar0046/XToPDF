package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.DrwFileConverter;

@AllArgsConstructor
@Component
public class DrwFileConverterFactory implements FileConverterFactory {
    private final DrwFileConverter drwFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return drwFileConverter;
    }
}
