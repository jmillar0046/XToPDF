package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.CatdrawingFileConverter;

@AllArgsConstructor
@Component
public class CatdrawingFileConverterFactory implements FileConverterFactory {
    private final CatdrawingFileConverter catdrawingFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return catdrawingFileConverter;
    }
}
