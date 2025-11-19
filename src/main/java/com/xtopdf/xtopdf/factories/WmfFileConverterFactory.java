package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.WmfFileConverter;

@AllArgsConstructor
@Component
public class WmfFileConverterFactory implements FileConverterFactory {
    private final WmfFileConverter wmfFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return wmfFileConverter;
    }
}
