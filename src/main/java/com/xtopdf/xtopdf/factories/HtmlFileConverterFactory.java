package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.HtmlFileConverter;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class HtmlFileConverterFactory implements FileConverterFactory{
    private HtmlFileConverter htmlFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return htmlFileConverter;
    }
}
