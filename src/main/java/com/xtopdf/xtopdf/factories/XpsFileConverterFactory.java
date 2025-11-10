package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.XpsFileConverter;

@AllArgsConstructor
@Component
public class XpsFileConverterFactory implements FileConverterFactory {
    private final XpsFileConverter xpsFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return xpsFileConverter;
    }
}
