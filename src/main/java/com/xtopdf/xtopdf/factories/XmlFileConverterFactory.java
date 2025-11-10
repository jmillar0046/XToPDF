package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.XmlFileConverter;

@AllArgsConstructor
@Component
public class XmlFileConverterFactory implements FileConverterFactory {
    private final XmlFileConverter xmlFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return xmlFileConverter;
    }
}
