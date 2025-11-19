package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.RvtFileConverter;

@AllArgsConstructor
@Component
public class RvtFileConverterFactory implements FileConverterFactory {
    private final RvtFileConverter rvtFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return rvtFileConverter;
    }
}
