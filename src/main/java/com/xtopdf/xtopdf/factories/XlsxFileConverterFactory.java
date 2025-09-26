package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.XlsxFileConverter;

@AllArgsConstructor
@Component
public class XlsxFileConverterFactory implements FileConverterFactory {
    private final XlsxFileConverter xlsxFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return xlsxFileConverter;
    }
}