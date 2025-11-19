package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.ObjFileConverter;

@AllArgsConstructor
@Component
public class ObjFileConverterFactory implements FileConverterFactory {
    private final ObjFileConverter objFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return objFileConverter;
    }
}
