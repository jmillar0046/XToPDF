package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.DwfFileConverter;

@AllArgsConstructor
@Component
public class DwfFileConverterFactory implements FileConverterFactory {
    private final DwfFileConverter dwfFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return dwfFileConverter;
    }
}
