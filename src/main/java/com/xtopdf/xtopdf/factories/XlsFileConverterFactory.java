package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.XlsFileConverter;

@AllArgsConstructor
@Component
public class XlsFileConverterFactory implements FileConverterFactory {
    private final XlsFileConverter xlsFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return xlsFileConverter;
    }
}
