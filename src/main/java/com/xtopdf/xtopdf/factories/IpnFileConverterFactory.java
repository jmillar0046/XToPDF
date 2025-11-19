package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.IpnFileConverter;

@AllArgsConstructor
@Component
public class IpnFileConverterFactory implements FileConverterFactory {
    private final IpnFileConverter ipnFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return ipnFileConverter;
    }
}
