package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.ThreeMfFileConverter;

@AllArgsConstructor
@Component
public class ThreeMfFileConverterFactory implements FileConverterFactory {
    private final ThreeMfFileConverter threemfFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return threemfFileConverter;
    }
}
