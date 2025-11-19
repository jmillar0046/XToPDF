package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.StepFileConverter;

@AllArgsConstructor
@Component
public class StepFileConverterFactory implements FileConverterFactory {
    private final StepFileConverter stepFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return stepFileConverter;
    }
}
