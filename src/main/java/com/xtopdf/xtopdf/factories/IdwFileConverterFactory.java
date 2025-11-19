package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.IdwFileConverter;

@AllArgsConstructor
@Component
public class IdwFileConverterFactory implements FileConverterFactory {
    private final IdwFileConverter idwFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return idwFileConverter;
    }
}
