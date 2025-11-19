package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.CatproductFileConverter;

@AllArgsConstructor
@Component
public class CatproductFileConverterFactory implements FileConverterFactory {
    private final CatproductFileConverter catproductFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return catproductFileConverter;
    }
}
