package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.OdpFileConverter;

@AllArgsConstructor
@Component
public class OdpFileConverterFactory implements FileConverterFactory {
    private final OdpFileConverter odpFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return odpFileConverter;
    }
}
