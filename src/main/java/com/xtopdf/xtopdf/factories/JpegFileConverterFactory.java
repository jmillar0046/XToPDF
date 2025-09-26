package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.JpegFileConverter;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class JpegFileConverterFactory implements FileConverterFactory {
    private final JpegFileConverter jpegFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return jpegFileConverter;
    }
}