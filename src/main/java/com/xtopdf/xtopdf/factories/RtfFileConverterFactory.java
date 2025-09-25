package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.RtfFileConverter;

@AllArgsConstructor
@Component
public class RtfFileConverterFactory implements FileConverterFactory {
    private final RtfFileConverter rtfFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return rtfFileConverter;
    }
}