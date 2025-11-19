package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.IamFileConverter;

@AllArgsConstructor
@Component
public class IamFileConverterFactory implements FileConverterFactory {
    private final IamFileConverter iamFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return iamFileConverter;
    }
}
