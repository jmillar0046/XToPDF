package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.RfaFileConverter;

@AllArgsConstructor
@Component
public class RfaFileConverterFactory implements FileConverterFactory {
    private final RfaFileConverter rfaFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return rfaFileConverter;
    }
}
