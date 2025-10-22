package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.SvgFileConverter;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class SvgFileConverterFactory implements FileConverterFactory {
    private final SvgFileConverter svgFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return svgFileConverter;
    }
}