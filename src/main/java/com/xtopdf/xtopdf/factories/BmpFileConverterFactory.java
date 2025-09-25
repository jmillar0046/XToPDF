package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.BmpFileConverter;
import com.xtopdf.xtopdf.converters.FileConverter;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class BmpFileConverterFactory implements FileConverterFactory {
    private final BmpFileConverter bmpFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return bmpFileConverter;
    }
}