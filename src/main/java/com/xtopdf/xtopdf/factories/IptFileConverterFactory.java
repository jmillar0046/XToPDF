package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.IptFileConverter;

@AllArgsConstructor
@Component
public class IptFileConverterFactory implements FileConverterFactory {
    private final IptFileConverter iptFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return iptFileConverter;
    }
}
