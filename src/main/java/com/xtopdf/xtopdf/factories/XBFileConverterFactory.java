package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.XBFileConverter;

@AllArgsConstructor
@Component
public class XBFileConverterFactory implements FileConverterFactory {
    private final XBFileConverter xbFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return xbFileConverter;
    }
}
